using StakeholdersService.DTOs;
using StakeholdersService.Models;
using StakeholdersService.Repositories;

namespace StakeholdersService.Services;

public class UserService(IUserRepository userRepository) : IUserService
{
    private const int MaxNameLength = 100;
    private const int MaxMottoLength = 500;

    public async Task<IReadOnlyCollection<UserResponseDto>> GetAllUsersAsync(CancellationToken cancellationToken = default)
    {
        var users = await userRepository.GetAllAsync(cancellationToken);

        return users
            .Select(user => new UserResponseDto
            {
                Id = user.Id,
                Username = user.Username,
                Email = user.Email,
                Role = user.Role.ToString(),
                IsBlocked = user.IsBlocked
            })
            .ToList();
    }

    public async Task<ServiceResult<UserResponseDto>> BlockUserAsync(int adminId, int userId, CancellationToken cancellationToken = default)
    {
        if (adminId <= 0)
        {
            return ServiceResult<UserResponseDto>.Failure(
                "Admin header X-Admin-Id je obavezan.",
                StatusCodes.Status401Unauthorized);
        }

        var admin = await userRepository.GetByIdAsync(adminId, cancellationToken);
        if (admin is null || admin.Role != UserRole.Admin)
        {
            return ServiceResult<UserResponseDto>.Failure(
                "Samo administrator moze da blokira korisnike.",
                StatusCodes.Status403Forbidden);
        }

        var targetUser = await userRepository.GetByIdAsync(userId, cancellationToken);
        if (targetUser is null)
        {
            return ServiceResult<UserResponseDto>.Failure(
                "Korisnik nije pronadjen.",
                StatusCodes.Status404NotFound);
        }

        if (targetUser.Role == UserRole.Admin)
        {
            return ServiceResult<UserResponseDto>.Failure(
                "Administratorski nalog se ne moze blokirati.",
                StatusCodes.Status400BadRequest);
        }

        if (!targetUser.IsBlocked)
        {
            targetUser.IsBlocked = true;
            await userRepository.SaveChangesAsync(cancellationToken);
        }

        return ServiceResult<UserResponseDto>.Success(
            new UserResponseDto
            {
                Id = targetUser.Id,
                Username = targetUser.Username,
                Email = targetUser.Email,
                Role = targetUser.Role.ToString(),
                IsBlocked = targetUser.IsBlocked
            },
            StatusCodes.Status200OK,
            "Korisnicki nalog je uspesno blokiran.");
    }

    public async Task<ServiceResult<UserProfileResponseDto>> GetMyProfileAsync(
        int requesterId,
        int userId,
        CancellationToken cancellationToken = default)
    {
        var userResult = await GetOwnNonAdminUserAsync(requesterId, userId, cancellationToken);
        if (!userResult.IsSuccess)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                userResult.Message,
                userResult.StatusCode);
        }

        return ServiceResult<UserProfileResponseDto>.Success(MapProfile(userResult.Data!));
    }

    public async Task<ServiceResult<UserProfileResponseDto>> UpdateMyProfileAsync(
        int requesterId,
        int userId,
        UpdateUserProfileDto request,
        CancellationToken cancellationToken = default)
    {
        var userResult = await GetOwnNonAdminUserAsync(requesterId, userId, cancellationToken);
        if (!userResult.IsSuccess)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                userResult.Message,
                userResult.StatusCode);
        }

        var validationError = TryNormalizeProfileData(
            request.FirstName,
            request.LastName,
            request.ProfileImage,
            request.Biography,
            request.Motto,
            out var firstName,
            out var lastName,
            out var profileImage,
            out var biography,
            out var motto);

        if (validationError is not null)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                validationError,
                StatusCodes.Status400BadRequest);
        }

        var user = userResult.Data!;
        ApplyProfileData(user, firstName, lastName, profileImage, biography, motto);
        user.IsProfileInitialized = true;

        await userRepository.SaveChangesAsync(cancellationToken);

        return ServiceResult<UserProfileResponseDto>.Success(
            MapProfile(user),
            StatusCodes.Status200OK,
            "Profil je uspesno izmenjen.");
    }

    public async Task<ServiceResult<UserProfileResponseDto>> InitializeMyProfileAsync(
        int requesterId,
        int userId,
        InitializeUserProfileDto request,
        CancellationToken cancellationToken = default)
    {
        var userResult = await GetOwnNonAdminUserAsync(requesterId, userId, cancellationToken);
        if (!userResult.IsSuccess)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                userResult.Message,
                userResult.StatusCode);
        }

        var user = userResult.Data!;
        if (user.IsProfileInitialized)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                "Profil je vec inicijalno popunjen. Izmena nije dozvoljena.",
                StatusCodes.Status409Conflict);
        }

        var validationError = TryNormalizeProfileData(
            request.FirstName,
            request.LastName,
            request.ProfileImage,
            request.Biography,
            request.Motto,
            out var firstName,
            out var lastName,
            out var profileImage,
            out var biography,
            out var motto);

        if (validationError is not null)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                validationError,
                StatusCodes.Status400BadRequest);
        }

        ApplyProfileData(user, firstName, lastName, profileImage, biography, motto);
        user.IsProfileInitialized = true;

        await userRepository.SaveChangesAsync(cancellationToken);

        return ServiceResult<UserProfileResponseDto>.Success(
            MapProfile(user),
            StatusCodes.Status201Created,
            "Profil je uspesno inicijalno popunjen.");
    }

    private async Task<ServiceResult<User>> GetOwnNonAdminUserAsync(
        int requesterId,
        int userId,
        CancellationToken cancellationToken)
    {
        if (requesterId <= 0)
        {
            return ServiceResult<User>.Failure(
                "User header X-User-Id je obavezan.",
                StatusCodes.Status401Unauthorized);
        }

        if (requesterId != userId)
        {
            return ServiceResult<User>.Failure(
                "Mozes pristupiti i menjati samo svoj profil.",
                StatusCodes.Status403Forbidden);
        }

        var user = await userRepository.GetByIdAsync(userId, cancellationToken);
        if (user is null)
        {
            return ServiceResult<User>.Failure(
                "Korisnik nije pronadjen.",
                StatusCodes.Status404NotFound);
        }

        if (user.Role == UserRole.Admin)
        {
            return ServiceResult<User>.Failure(
                "Administratorski profil ne koristi ovu funkcionalnost.",
                StatusCodes.Status403Forbidden);
        }

        return ServiceResult<User>.Success(user);
    }

    private static string? TryNormalizeProfileData(
        string firstNameRaw,
        string lastNameRaw,
        string profileImageRaw,
        string biographyRaw,
        string mottoRaw,
        out string firstName,
        out string lastName,
        out string profileImage,
        out string biography,
        out string motto)
    {
        firstName = firstNameRaw.Trim();
        lastName = lastNameRaw.Trim();
        profileImage = profileImageRaw.Trim();
        biography = biographyRaw.Trim();
        motto = mottoRaw.Trim();

        if (string.IsNullOrWhiteSpace(firstName) ||
            string.IsNullOrWhiteSpace(lastName) ||
            string.IsNullOrWhiteSpace(profileImage) ||
            string.IsNullOrWhiteSpace(biography) ||
            string.IsNullOrWhiteSpace(motto))
        {
            return "FirstName, LastName, ProfileImage, Biography i Motto su obavezni.";
        }

        if (firstName.Length > MaxNameLength || lastName.Length > MaxNameLength)
        {
            return "FirstName i LastName mogu imati najvise 100 karaktera.";
        }

        if (motto.Length > MaxMottoLength)
        {
            return "Motto moze imati najvise 500 karaktera.";
        }

        return null;
    }

    private static void ApplyProfileData(
        User user,
        string firstName,
        string lastName,
        string profileImage,
        string biography,
        string motto)
    {
        user.FirstName = firstName;
        user.LastName = lastName;
        user.ProfileImage = profileImage;
        user.Biography = biography;
        user.Motto = motto;
    }

    private static UserProfileResponseDto MapProfile(User user)
    {
        return new UserProfileResponseDto
        {
            UserId = user.Id,
            FirstName = user.FirstName ?? string.Empty,
            LastName = user.LastName ?? string.Empty,
            ProfileImage = user.ProfileImage ?? string.Empty,
            Biography = user.Biography ?? string.Empty,
            Motto = user.Motto ?? string.Empty,
            IsProfileInitialized = user.IsProfileInitialized
        };
    }
}
