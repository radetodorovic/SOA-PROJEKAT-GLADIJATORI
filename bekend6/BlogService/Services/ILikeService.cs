using BlogService.DTOs;

namespace BlogService.Services;

public interface ILikeService
{
    Task<ServiceResult<object>> LikeBlogAsync(int blogId, int userId, CancellationToken cancellationToken = default);
    Task<ServiceResult<object>> UnlikeBlogAsync(int blogId, int userId, CancellationToken cancellationToken = default);
    Task<ServiceResult<int>> GetLikesCountAsync(int blogId, CancellationToken cancellationToken = default);
    Task<ServiceResult<bool>> IsLikedByUserAsync(int blogId, int userId, CancellationToken cancellationToken = default);
}
