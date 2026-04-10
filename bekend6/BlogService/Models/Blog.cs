namespace BlogService.Models;

public class Blog
{
    public int Id { get; set; }
    public int AuthorId { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public DateTime CreatedAtUtc { get; set; }
    public ICollection<BlogImage> Images { get; set; } = [];
    public ICollection<BlogComment> Comments { get; set; } = [];
}
