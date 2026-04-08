using StakeholdersService.DTOs;

namespace StakeholdersService.Services;

public interface IUserService
{
    Task<IReadOnlyCollection<UserResponseDto>> GetAllUsersAsync(CancellationToken cancellationToken = default);
    Task<ServiceResult<UserResponseDto>> BlockUserAsync(int adminId, int userId, CancellationToken cancellationToken = default);
}
