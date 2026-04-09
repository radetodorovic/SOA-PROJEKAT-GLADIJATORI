using Microsoft.EntityFrameworkCore;
using BlogService.Data;
using BlogService.Models;

namespace BlogService.Repositories;

public class BlogRepository(BlogDbContext dbContext) : IBlogRepository
{
    public async Task<IReadOnlyCollection<Blog>> GetAllAsync(CancellationToken cancellationToken = default)
    {
        return await dbContext.Blogs
            .AsNoTracking()
            .Include(blog => blog.Images.OrderBy(image => image.OrderIndex))
            .OrderByDescending(blog => blog.CreatedAtUtc)
            .ToListAsync(cancellationToken);
    }

    public async Task<Blog?> GetByIdAsync(int id, CancellationToken cancellationToken = default)
    {
        return await dbContext.Blogs
            .AsNoTracking()
            .Include(blog => blog.Images.OrderBy(image => image.OrderIndex))
            .FirstOrDefaultAsync(blog => blog.Id == id, cancellationToken);
    }

    public async Task<Blog> AddAsync(Blog blog, CancellationToken cancellationToken = default)
    {
        dbContext.Blogs.Add(blog);
        await dbContext.SaveChangesAsync(cancellationToken);
        return blog;
    }
}
