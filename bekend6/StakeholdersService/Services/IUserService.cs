using StakeholdersService.DTOs;

namespace StakeholdersService.Services;

public interface IUserService
{
    Task<IReadOnlyCollection<UserResponseDto>> GetAllUsersAsync(CancellationToken cancellationToken = default);
}
