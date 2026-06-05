namespace StakeholdersService.DTOs;

public class UserActivityResponseDto
{
    public int Id { get; init; }
    public int UserId { get; init; }
    public string ActivityType { get; init; } = string.Empty;
    public string ReferenceId { get; init; } = string.Empty;
    public string Description { get; init; } = string.Empty;
    public DateTime CreatedAt { get; init; }
}
