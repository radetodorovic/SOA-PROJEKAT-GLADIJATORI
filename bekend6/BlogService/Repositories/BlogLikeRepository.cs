using Microsoft.EntityFrameworkCore;
using BlogService.Data;
using BlogService.Models;

namespace BlogService.Repositories;

public class BlogLikeRepository(BlogDbContext dbContext) : IBlogLikeRepository
{
    public async Task<BlogLike?> GetLikeAsync(
        int blogId,
        int userId,
        CancellationToken cancellationToken = default)
    {
        return await dbContext.BlogLikes
            .AsNoTracking()
            .FirstOrDefaultAsync(
                like => like.BlogId == blogId && like.UserId == userId,
                cancellationToken);
    }

    public async Task<int> GetLikesCountAsync(int blogId, CancellationToken cancellationToken = default)
    {
        return await dbContext.BlogLikes
            .AsNoTracking()
            .CountAsync(like => like.BlogId == blogId, cancellationToken);
    }

    public async Task<BlogLike> AddLikeAsync(BlogLike like, CancellationToken cancellationToken = default)
    {
        dbContext.BlogLikes.Add(like);
        await dbContext.SaveChangesAsync(cancellationToken);
        return like;
    }

    public async Task<bool> RemoveLikeAsync(int blogId, int userId, CancellationToken cancellationToken = default)
    {
        var like = await dbContext.BlogLikes
            .FirstOrDefaultAsync(
                l => l.BlogId == blogId && l.UserId == userId,
                cancellationToken);

        if (like is null)
            return false;

        dbContext.BlogLikes.Remove(like);
        await dbContext.SaveChangesAsync(cancellationToken);
        return true;
    }
}
