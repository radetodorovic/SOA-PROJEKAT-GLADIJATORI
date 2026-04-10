using BlogService.Models;

namespace BlogService.Repositories;

public interface IBlogLikeRepository
{
    Task<BlogLike?> GetLikeAsync(int blogId, int userId, CancellationToken cancellationToken = default);
    Task<int> GetLikesCountAsync(int blogId, CancellationToken cancellationToken = default);
    Task<BlogLike> AddLikeAsync(BlogLike like, CancellationToken cancellationToken = default);
    Task<bool> RemoveLikeAsync(int blogId, int userId, CancellationToken cancellationToken = default);
}
