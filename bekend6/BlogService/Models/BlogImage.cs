namespace BlogService.Models;

public class BlogImage
{
    public int Id { get; set; }
    public int BlogId { get; set; }
    public string Url { get; set; } = string.Empty;
    public int OrderIndex { get; set; }
    public Blog? Blog { get; set; }
}
