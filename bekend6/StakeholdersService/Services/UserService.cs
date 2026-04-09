using StakeholdersService.DTOs;
using StakeholdersService.Models;
using StakeholdersService.Repositories;

namespace StakeholdersService.Services;

public class UserService(IUserRepository userRepository) : IUserService
{
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
        if (requesterId <= 0)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                "User header X-User-Id je obavezan.",
                StatusCodes.Status401Unauthorized);
        }

        if (requesterId != userId)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                "Mozes pristupiti samo svom profilu.",
                StatusCodes.Status403Forbidden);
        }

        var user = await userRepository.GetByIdAsync(userId, cancellationToken);
        if (user is null)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                "Korisnik nije pronadjen.",
                StatusCodes.Status404NotFound);
        }

        if (user.Role == UserRole.Admin)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                "Administratorski profil ne koristi ovu funkcionalnost.",
                StatusCodes.Status403Forbidden);
        }

        return ServiceResult<UserProfileResponseDto>.Success(MapProfile(user));
    }

    public async Task<ServiceResult<UserProfileResponseDto>> InitializeMyProfileAsync(
        int requesterId,
        int userId,
        InitializeUserProfileDto request,
        CancellationToken cancellationToken = default)
    {
        if (requesterId <= 0)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                "User header X-User-Id je obavezan.",
                StatusCodes.Status401Unauthorized);
        }

        if (requesterId != userId)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                "Mozes menjati samo svoj profil.",
                StatusCodes.Status403Forbidden);
        }

        var user = await userRepository.GetByIdAsync(userId, cancellationToken);
        if (user is null)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                "Korisnik nije pronadjen.",
                StatusCodes.Status404NotFound);
        }

        if (user.Role == UserRole.Admin)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                "Administratorski profil ne koristi ovu funkcionalnost.",
                StatusCodes.Status403Forbidden);
        }

        if (user.IsProfileInitialized)
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                "Profil je vec inicijalno popunjen. Izmena nije dozvoljena.",
                StatusCodes.Status409Conflict);
        }

        var firstName = request.FirstName.Trim();
        var lastName = request.LastName.Trim();
        var profileImage = request.ProfileImage.Trim();
        var biography = request.Biography.Trim();
        var motto = request.Motto.Trim();

        if (string.IsNullOrWhiteSpace(firstName) ||
            string.IsNullOrWhiteSpace(lastName) ||
            string.IsNullOrWhiteSpace(profileImage) ||
            string.IsNullOrWhiteSpace(biography) ||
            string.IsNullOrWhiteSpace(motto))
        {
            return ServiceResult<UserProfileResponseDto>.Failure(
                "FirstName, LastName, ProfileImage, Biography i Motto su obavezni.",
                StatusCodes.Status400BadRequest);
        }

        user.FirstName = firstName;
        user.LastName = lastName;
        user.ProfileImage = profileImage;
        user.Biography = biography;
        user.Motto = motto;
        user.IsProfileInitialized = true;

        await userRepository.SaveChangesAsync(cancellationToken);

        return ServiceResult<UserProfileResponseDto>.Success(
            MapProfile(user),
            StatusCodes.Status201Created,
            "Profil je uspesno inicijalno popunjen.");
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
