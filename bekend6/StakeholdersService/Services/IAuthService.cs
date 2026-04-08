using StakeholdersService.DTOs;

namespace StakeholdersService.Services;

public interface IAuthService
{
    Task<ServiceResult<RegisteredUserDto>> RegisterAsync(RegisterUserDto request, CancellationToken cancellationToken = default);
    Task<ServiceResult<LoginResponseDto>> LoginAsync(LoginUserDto request, CancellationToken cancellationToken = default);
}
