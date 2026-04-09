using BlogService.Models;

namespace BlogService.Repositories;

public interface IBlogRepository
{
    Task<IReadOnlyCollection<Blog>> GetAllAsync(CancellationToken cancellationToken = default);
    Task<Blog?> GetByIdAsync(int id, CancellationToken cancellationToken = default);
    Task<Blog> AddAsync(Blog blog, CancellationToken cancellationToken = default);
}
