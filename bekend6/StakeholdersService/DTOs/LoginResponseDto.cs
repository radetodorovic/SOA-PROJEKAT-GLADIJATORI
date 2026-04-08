namespace StakeholdersService.DTOs;

public class LoginResponseDto
{
    public int Id { get; init; }
    public string Username { get; init; } = string.Empty;
    public string Email { get; init; } = string.Empty;
    public string Role { get; init; } = string.Empty;
    public bool IsBlocked { get; init; }
    public string Message { get; init; } = string.Empty;
}
