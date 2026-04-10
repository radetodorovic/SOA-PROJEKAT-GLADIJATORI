using BlogService.Models;

namespace BlogService.Repositories;

public interface IBlogCommentRepository
{
    Task<IReadOnlyCollection<BlogComment>> GetByBlogIdAsync(int blogId, CancellationToken cancellationToken = default);
    Task<BlogComment?> GetByIdAsync(int id, CancellationToken cancellationToken = default);
    Task<BlogComment> AddAsync(BlogComment comment, CancellationToken cancellationToken = default);
    Task<BlogComment?> UpdateAsync(int id, BlogComment comment, CancellationToken cancellationToken = default);
    Task<bool> DeleteAsync(int id, CancellationToken cancellationToken = default);
}
