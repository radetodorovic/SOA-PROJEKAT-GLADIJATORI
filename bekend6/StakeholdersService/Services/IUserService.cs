using StakeholdersService.DTOs;

namespace StakeholdersService.Services;

public interface IUserService
{
    Task<IReadOnlyCollection<UserResponseDto>> GetAllUsersAsync(CancellationToken cancellationToken = default);
    Task<ServiceResult<UserResponseDto>> BlockUserAsync(int adminId, int userId, CancellationToken cancellationToken = default);
    Task<ServiceResult<UserProfileResponseDto>> GetMyProfileAsync(int requesterId, int userId, CancellationToken cancellationToken = default);
    Task<ServiceResult<UserProfileResponseDto>> InitializeMyProfileAsync(
        int requesterId,
        int userId,
        InitializeUserProfileDto request,
        CancellationToken cancellationToken = default);
}
