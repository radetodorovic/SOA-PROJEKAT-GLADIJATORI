namespace StakeholdersService.DTOs;

public class UserProfileResponseDto
{
    public int UserId { get; init; }
    public string FirstName { get; init; } = string.Empty;
    public string LastName { get; init; } = string.Empty;
    public string ProfileImage { get; init; } = string.Empty;
    public string Biography { get; init; } = string.Empty;
    public string Motto { get; init; } = string.Empty;
    public bool IsProfileInitialized { get; init; }
}
