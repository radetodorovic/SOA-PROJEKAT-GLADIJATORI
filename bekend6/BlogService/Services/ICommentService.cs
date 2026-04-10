using BlogService.DTOs;

namespace BlogService.Services;

public interface ICommentService
{
    Task<IReadOnlyCollection<CommentResponseDto>> GetCommentsByBlogIdAsync(
        int blogId,
        CancellationToken cancellationToken = default);
    Task<ServiceResult<CommentResponseDto>> GetCommentByIdAsync(int id, CancellationToken cancellationToken = default);
    Task<ServiceResult<CommentResponseDto>> CreateCommentAsync(
        int blogId,
        int userId,
        CreateCommentDto request,
        CancellationToken cancellationToken = default);
    Task<ServiceResult<CommentResponseDto>> UpdateCommentAsync(
        int commentId,
        int userId,
        CreateCommentDto request,
        CancellationToken cancellationToken = default);
    Task<ServiceResult<bool>> DeleteCommentAsync(int commentId, int userId, CancellationToken cancellationToken = default);
}
