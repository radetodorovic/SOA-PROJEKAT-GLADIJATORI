namespace BlogService.DTOs;

public class CreateBlogDto
{
    public string Title { get; init; } = string.Empty;
    public string Description { get; init; } = string.Empty;
    public IReadOnlyCollection<string> Images { get; init; } = [];
}
