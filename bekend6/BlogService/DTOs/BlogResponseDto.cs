namespace BlogService.DTOs;

public class BlogResponseDto
{
    public int Id { get; init; }
    public int AuthorId { get; init; }
    public string Title { get; init; } = string.Empty;
    public string Description { get; init; } = string.Empty;
    public DateTime CreatedAtUtc { get; init; }
    public string DescriptionFormat { get; init; } = "markdown";
    public IReadOnlyCollection<string> Images { get; init; } = [];
    public IReadOnlyCollection<CommentResponseDto> Comments { get; init; } = [];
}
