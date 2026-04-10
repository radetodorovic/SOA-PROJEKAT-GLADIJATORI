namespace BlogService.Models;

public class BlogComment
{
    public int Id { get; set; }
    public int BlogId { get; set; }
    public int UserId { get; set; }
    public string Text { get; set; } = string.Empty;
    public DateTime CreatedAtUtc { get; set; }
    public DateTime UpdatedAtUtc { get; set; }
    public Blog? Blog { get; set; }
}
