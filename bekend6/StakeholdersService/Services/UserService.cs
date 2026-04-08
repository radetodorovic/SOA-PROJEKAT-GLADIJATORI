using StakeholdersService.DTOs;
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
}
