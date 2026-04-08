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
}
