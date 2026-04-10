using BlogService.DTOs;
using BlogService.Models;
using BlogService.Repositories;

namespace BlogService.Services;

public class CommentService(
    IBlogCommentRepository commentRepository,
    IBlogRepository blogRepository) : ICommentService
{
    private const int MaxCommentLength = 5000;

    public async Task<IReadOnlyCollection<CommentResponseDto>> GetCommentsByBlogIdAsync(
        int blogId,
        CancellationToken cancellationToken = default)
    {
        var comments = await commentRepository.GetByBlogIdAsync(blogId, cancellationToken);
        return comments.Select(MapComment).ToList();
    }

    public async Task<ServiceResult<CommentResponseDto>> GetCommentByIdAsync(
        int id,
        CancellationToken cancellationToken = default)
    {
        if (id <= 0)
        {
            return ServiceResult<CommentResponseDto>.Failure(
                "Id komentara mora biti pozitivan broj.",
                StatusCodes.Status400BadRequest);
        }

        var comment = await commentRepository.GetByIdAsync(id, cancellationToken);
        if (comment is null)
        {
            return ServiceResult<CommentResponseDto>.Failure(
                "Komentar nije prona?en.",
                StatusCodes.Status404NotFound);
        }

        return ServiceResult<CommentResponseDto>.Success(MapComment(comment));
    }

    public async Task<ServiceResult<CommentResponseDto>> CreateCommentAsync(
        int blogId,
        int userId,
        CreateCommentDto request,
        CancellationToken cancellationToken = default)
    {
        if (blogId <= 0)
        {
            return ServiceResult<CommentResponseDto>.Failure(
                "Id bloga mora biti pozitivan broj.",
                StatusCodes.Status400BadRequest);
        }

        if (userId <= 0)
        {
            return ServiceResult<CommentResponseDto>.Failure(
                "User header X-User-Id je obavezan.",
                StatusCodes.Status401Unauthorized);
        }

        var blog = await blogRepository.GetByIdAsync(blogId, cancellationToken);
        if (blog is null)
        {
            return ServiceResult<CommentResponseDto>.Failure(
                "Blog nije prona?en.",
                StatusCodes.Status404NotFound);
        }

        var text = request.Text.Trim();

        if (string.IsNullOrWhiteSpace(text))
        {
            return ServiceResult<CommentResponseDto>.Failure(
                "Tekst komentara je obavezan.",
                StatusCodes.Status400BadRequest);
        }

        if (text.Length > MaxCommentLength)
        {
            return ServiceResult<CommentResponseDto>.Failure(
                "Komentar može imati maksimalno 5000 karaktera.",
                StatusCodes.Status400BadRequest);
        }

        var now = DateTime.UtcNow;
        var comment = new BlogComment
        {
            BlogId = blogId,
            UserId = userId,
            Text = text,
            CreatedAtUtc = now,
            UpdatedAtUtc = now
        };

        var savedComment = await commentRepository.AddAsync(comment, cancellationToken);

        return ServiceResult<CommentResponseDto>.Success(
            MapComment(savedComment),
            StatusCodes.Status201Created,
            "Komentar je uspešno kreiran.");
    }

    public async Task<ServiceResult<CommentResponseDto>> UpdateCommentAsync(
        int commentId,
        int userId,
        CreateCommentDto request,
        CancellationToken cancellationToken = default)
    {
        if (commentId <= 0)
        {
            return ServiceResult<CommentResponseDto>.Failure(
                "Id komentara mora biti pozitivan broj.",
                StatusCodes.Status400BadRequest);
        }

        if (userId <= 0)
        {
            return ServiceResult<CommentResponseDto>.Failure(
                "User header X-User-Id je obavezan.",
                StatusCodes.Status401Unauthorized);
        }

        var existingComment = await commentRepository.GetByIdAsync(commentId, cancellationToken);
        if (existingComment is null)
        {
            return ServiceResult<CommentResponseDto>.Failure(
                "Komentar nije prona?en.",
                StatusCodes.Status404NotFound);
        }

        if (existingComment.UserId != userId)
        {
            return ServiceResult<CommentResponseDto>.Failure(
                "Niste autorizovani da ažurirate ovaj komentar.",
                StatusCodes.Status403Forbidden);
        }

        var text = request.Text.Trim();

        if (string.IsNullOrWhiteSpace(text))
        {
            return ServiceResult<CommentResponseDto>.Failure(
                "Tekst komentara je obavezan.",
                StatusCodes.Status400BadRequest);
        }

        if (text.Length > MaxCommentLength)
        {
            return ServiceResult<CommentResponseDto>.Failure(
                "Komentar može imati maksimalno 5000 karaktera.",
                StatusCodes.Status400BadRequest);
        }

        var updatedComment = new BlogComment
        {
            Id = commentId,
            BlogId = existingComment.BlogId,
            UserId = existingComment.UserId,
            Text = text,
            CreatedAtUtc = existingComment.CreatedAtUtc,
            UpdatedAtUtc = DateTime.UtcNow
        };

        var result = await commentRepository.UpdateAsync(commentId, updatedComment, cancellationToken);

        if (result is null)
        {
            return ServiceResult<CommentResponseDto>.Failure(
                "Komentar nije prona?en.",
                StatusCodes.Status404NotFound);
        }

        return ServiceResult<CommentResponseDto>.Success(
            MapComment(result),
            StatusCodes.Status200OK,
            "Komentar je uspešno ažuriran.");
    }

    public async Task<ServiceResult<bool>> DeleteCommentAsync(
        int commentId,
        int userId,
        CancellationToken cancellationToken = default)
    {
        if (commentId <= 0)
        {
            return ServiceResult<bool>.Failure(
                "Id komentara mora biti pozitivan broj.",
                StatusCodes.Status400BadRequest);
        }

        if (userId <= 0)
        {
            return ServiceResult<bool>.Failure(
                "User header X-User-Id je obavezan.",
                StatusCodes.Status401Unauthorized);
        }

        var comment = await commentRepository.GetByIdAsync(commentId, cancellationToken);
        if (comment is null)
        {
            return ServiceResult<bool>.Failure(
                "Komentar nije prona?en.",
                StatusCodes.Status404NotFound);
        }

        if (comment.UserId != userId)
        {
            return ServiceResult<bool>.Failure(
                "Niste autorizovani da obrišete ovaj komentar.",
                StatusCodes.Status403Forbidden);
        }

        var deleted = await commentRepository.DeleteAsync(commentId, cancellationToken);

        if (!deleted)
        {
            return ServiceResult<bool>.Failure(
                "Komentar nije prona?en.",
                StatusCodes.Status404NotFound);
        }

        return ServiceResult<bool>.Success(
            true,
            StatusCodes.Status200OK,
            "Komentar je uspešno obrisan.");
    }

    private static CommentResponseDto MapComment(BlogComment comment)
    {
        return new CommentResponseDto
        {
            Id = comment.Id,
            BlogId = comment.BlogId,
            UserId = comment.UserId,
            Text = comment.Text,
            CreatedAtUtc = comment.CreatedAtUtc,
            UpdatedAtUtc = comment.UpdatedAtUtc
        };
    }
}
