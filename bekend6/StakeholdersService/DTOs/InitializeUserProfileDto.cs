namespace StakeholdersService.DTOs;

public class InitializeUserProfileDto
{
    public string FirstName { get; init; } = string.Empty;
    public string LastName { get; init; } = string.Empty;
    public string ProfileImage { get; init; } = string.Empty;
    public string Biography { get; init; } = string.Empty;
    public string Motto { get; init; } = string.Empty;
}
