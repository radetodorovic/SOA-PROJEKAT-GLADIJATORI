namespace StakeholdersService.Models;

public class UserActivityLog
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public string ActivityType { get; set; } = string.Empty;
    public string ReferenceId { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; }
}
