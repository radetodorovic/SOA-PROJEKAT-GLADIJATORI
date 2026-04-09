using BlogService.DTOs;

namespace BlogService.Services;

public interface IBlogService
{
    Task<IReadOnlyCollection<BlogResponseDto>> GetAllBlogsAsync(CancellationToken cancellationToken = default);
    Task<ServiceResult<BlogResponseDto>> GetBlogByIdAsync(int blogId, CancellationToken cancellationToken = default);
    Task<ServiceResult<BlogResponseDto>> CreateBlogAsync(int authorId, CreateBlogDto request, CancellationToken cancellationToken = default);
}
