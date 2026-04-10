using BlogService.DTOs;
using BlogService.Models;
using BlogService.Repositories;

namespace BlogService.Services;

public class LikeService(
    IBlogLikeRepository likeRepository,
    IBlogRepository blogRepository) : ILikeService
{
    public async Task<ServiceResult<object>> LikeBlogAsync(
        int blogId,
        int userId,
        CancellationToken cancellationToken = default)
    {
        if (blogId <= 0)
        {
            return ServiceResult<object>.Failure(
                "Id bloga mora biti pozitivan broj.",
                StatusCodes.Status400BadRequest);
        }

        if (userId <= 0)
        {
            return ServiceResult<object>.Failure(
                "User header X-User-Id je obavezan.",
                StatusCodes.Status401Unauthorized);
        }

        // Provjera da li blog postoji
        var blog = await blogRepository.GetByIdAsync(blogId, cancellationToken);
        if (blog is null)
        {
            return ServiceResult<object>.Failure(
                "Blog nije prona?en.",
                StatusCodes.Status404NotFound);
        }

        // Provjera da li je korisnik ve? lajkovao
        var existingLike = await likeRepository.GetLikeAsync(blogId, userId, cancellationToken);
        if (existingLike is not null)
        {
            return ServiceResult<object>.Failure(
                "Ve? ste lajkovali ovaj blog.",
                StatusCodes.Status409Conflict);
        }

        // Kreiraj novi lajk
        var like = new BlogLike
        {
            BlogId = blogId,
            UserId = userId,
            CreatedAtUtc = DateTime.UtcNow
        };

        await likeRepository.AddLikeAsync(like, cancellationToken);

        // Vrati broj lajkova
        var likesCount = await likeRepository.GetLikesCountAsync(blogId, cancellationToken);

        return ServiceResult<object>.Success(
            new { likesCount },
            StatusCodes.Status201Created,
            "Lajk je uspješno dodan.");
    }

    public async Task<ServiceResult<object>> UnlikeBlogAsync(
        int blogId,
        int userId,
        CancellationToken cancellationToken = default)
    {
        if (blogId <= 0)
        {
            return ServiceResult<object>.Failure(
                "Id bloga mora biti pozitivan broj.",
                StatusCodes.Status400BadRequest);
        }

        if (userId <= 0)
        {
            return ServiceResult<object>.Failure(
                "User header X-User-Id je obavezan.",
                StatusCodes.Status401Unauthorized);
        }

        // Provjera da li blog postoji
        var blog = await blogRepository.GetByIdAsync(blogId, cancellationToken);
        if (blog is null)
        {
            return ServiceResult<object>.Failure(
                "Blog nije prona?en.",
                StatusCodes.Status404NotFound);
        }

        // Ukloniti lajk
        var removed = await likeRepository.RemoveLikeAsync(blogId, userId, cancellationToken);
        if (!removed)
        {
            return ServiceResult<object>.Failure(
                "Niste lajkovali ovaj blog.",
                StatusCodes.Status400BadRequest);
        }

        // Vrati broj lajkova
        var likesCount = await likeRepository.GetLikesCountAsync(blogId, cancellationToken);

        return ServiceResult<object>.Success(
            new { likesCount },
            StatusCodes.Status200OK,
            "Lajk je uspješno uklonjen.");
    }

    public async Task<ServiceResult<int>> GetLikesCountAsync(
        int blogId,
        CancellationToken cancellationToken = default)
    {
        if (blogId <= 0)
        {
            return ServiceResult<int>.Failure(
                "Id bloga mora biti pozitivan broj.",
                StatusCodes.Status400BadRequest);
        }

        var likesCount = await likeRepository.GetLikesCountAsync(blogId, cancellationToken);
        return ServiceResult<int>.Success(likesCount);
    }

    public async Task<ServiceResult<bool>> IsLikedByUserAsync(
        int blogId,
        int userId,
        CancellationToken cancellationToken = default)
    {
        if (blogId <= 0)
        {
            return ServiceResult<bool>.Failure(
                "Id bloga mora biti pozitivan broj.",
                StatusCodes.Status400BadRequest);
        }

        if (userId <= 0)
        {
            return ServiceResult<bool>.Failure(
                "User header X-User-Id je obavezan.",
                StatusCodes.Status401Unauthorized);
        }

        var like = await likeRepository.GetLikeAsync(blogId, userId, cancellationToken);
        return ServiceResult<bool>.Success(like is not null);
    }
}
