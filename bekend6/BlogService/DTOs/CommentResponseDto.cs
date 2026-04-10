namespace BlogService.DTOs;

public class CommentResponseDto
{
    public int Id { get; init; }
    public int BlogId { get; init; }
    public int UserId { get; init; }
    public string Text { get; init; } = string.Empty;
    public DateTime CreatedAtUtc { get; init; }
    public DateTime UpdatedAtUtc { get; init; }
}
