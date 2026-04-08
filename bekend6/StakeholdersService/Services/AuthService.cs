using StakeholdersService.DTOs;
using StakeholdersService.Models;
using StakeholdersService.Repositories;

namespace StakeholdersService.Services;

public class AuthService(IUserRepository userRepository) : IAuthService
{
    public async Task<ServiceResult<RegisteredUserDto>> RegisterAsync(RegisterUserDto request, CancellationToken cancellationToken = default)
    {
        var username = request.Username.Trim();
        var email = request.Email.Trim().ToLower();
        var password = request.Password.Trim();
        var roleRaw = request.Role.Trim();

        if (string.IsNullOrWhiteSpace(username) ||
            string.IsNullOrWhiteSpace(email) ||
            string.IsNullOrWhiteSpace(password) ||
            string.IsNullOrWhiteSpace(roleRaw))
        {
            return ServiceResult<RegisteredUserDto>.Failure(
                "Username, email, password and role are required.",
                StatusCodes.Status400BadRequest);
        }

        if (password.Length < 8)
        {
            return ServiceResult<RegisteredUserDto>.Failure(
                "Password must contain at least 8 characters.",
                StatusCodes.Status400BadRequest);
        }

        if (!Enum.TryParse<UserRole>(roleRaw, true, out var role) || role == UserRole.Admin)
        {
            return ServiceResult<RegisteredUserDto>.Failure(
                "Only Guide and Tourist roles are allowed during registration.",
                StatusCodes.Status400BadRequest);
        }

        if (await userRepository.GetByEmailAsync(email, cancellationToken) is not null)
        {
            return ServiceResult<RegisteredUserDto>.Failure(
                "An account with this email already exists.",
                StatusCodes.Status409Conflict);
        }

        if (await userRepository.GetByUsernameAsync(username, cancellationToken) is not null)
        {
            return ServiceResult<RegisteredUserDto>.Failure(
                "This username is already taken.",
                StatusCodes.Status409Conflict);
        }

        var user = new User
        {
            Username = username,
            Email = email,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(password),
            Role = role,
            IsBlocked = false
        };

        var savedUser = await userRepository.AddAsync(user, cancellationToken);

        return ServiceResult<RegisteredUserDto>.Success(
            new RegisteredUserDto
            {
                Id = savedUser.Id,
                Username = savedUser.Username,
                Email = savedUser.Email,
                Role = savedUser.Role.ToString(),
                IsBlocked = savedUser.IsBlocked
            },
            StatusCodes.Status201Created,
            "User registered successfully.");
    }

    public async Task<ServiceResult<LoginResponseDto>> LoginAsync(LoginUserDto request, CancellationToken cancellationToken = default)
    {
        var identifier = request.Identifier.Trim();
        var password = request.Password.Trim();

        if (string.IsNullOrWhiteSpace(identifier) || string.IsNullOrWhiteSpace(password))
        {
            return ServiceResult<LoginResponseDto>.Failure(
                "Username/email and password are required.",
                StatusCodes.Status400BadRequest);
        }

        var user = await userRepository.GetByEmailOrUsernameAsync(identifier, cancellationToken);
        if (user is null || !BCrypt.Net.BCrypt.Verify(password, user.PasswordHash))
        {
            return ServiceResult<LoginResponseDto>.Failure(
                "Invalid credentials.",
                StatusCodes.Status401Unauthorized);
        }

        if (user.IsBlocked)
        {
            return ServiceResult<LoginResponseDto>.Failure(
                "This account is blocked.",
                StatusCodes.Status403Forbidden);
        }

        return ServiceResult<LoginResponseDto>.Success(
            new LoginResponseDto
            {
                Id = user.Id,
                Username = user.Username,
                Email = user.Email,
                Role = user.Role.ToString(),
                IsBlocked = user.IsBlocked,
                Message = $"Login successful. Welcome {user.Username}."
            });
    }
}
