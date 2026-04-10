using Microsoft.EntityFrameworkCore;
using BlogService.Data;
using BlogService.Models;

namespace BlogService.Repositories;

public class BlogCommentRepository(BlogDbContext dbContext) : IBlogCommentRepository
{
    public async Task<IReadOnlyCollection<BlogComment>> GetByBlogIdAsync(
        int blogId,
        CancellationToken cancellationToken = default)
    {
        return await dbContext.BlogComments
            .AsNoTracking()
            .Where(comment => comment.BlogId == blogId)
            .OrderByDescending(comment => comment.CreatedAtUtc)
            .ToListAsync(cancellationToken);
    }

    public async Task<BlogComment?> GetByIdAsync(int id, CancellationToken cancellationToken = default)
    {
        return await dbContext.BlogComments
            .AsNoTracking()
            .FirstOrDefaultAsync(comment => comment.Id == id, cancellationToken);
    }

    public async Task<BlogComment> AddAsync(BlogComment comment, CancellationToken cancellationToken = default)
    {
        dbContext.BlogComments.Add(comment);
        await dbContext.SaveChangesAsync(cancellationToken);
        return comment;
    }

    public async Task<BlogComment?> UpdateAsync(int id, BlogComment comment, CancellationToken cancellationToken = default)
    {
        var existingComment = await dbContext.BlogComments.FirstOrDefaultAsync(
            c => c.Id == id,
            cancellationToken);

        if (existingComment is null)
            return null;

        existingComment.Text = comment.Text;
        existingComment.UpdatedAtUtc = DateTime.UtcNow;

        dbContext.BlogComments.Update(existingComment);
        await dbContext.SaveChangesAsync(cancellationToken);
        return existingComment;
    }

    public async Task<bool> DeleteAsync(int id, CancellationToken cancellationToken = default)
    {
        var comment = await dbContext.BlogComments.FirstOrDefaultAsync(
            c => c.Id == id,
            cancellationToken);

        if (comment is null)
            return false;

        dbContext.BlogComments.Remove(comment);
        await dbContext.SaveChangesAsync(cancellationToken);
        return true;
    }
}
