using BlogService.DTOs;
using BlogService.Models;
using BlogService.Repositories;

namespace BlogService.Services;

public class BlogService(
    IBlogRepository blogRepository,
    IFollowerClient followerClient) : IBlogService
{
    private const int MaxTitleLength = 200;

    public async Task<IReadOnlyCollection<BlogResponseDto>> GetAllBlogsAsync(CancellationToken cancellationToken = default)
    {
        var blogs = await blogRepository.GetAllAsync(cancellationToken);
        return blogs.Select(blog => MapBlog(blog, userId: null)).ToList();
    }

    public async Task<ServiceResult<IReadOnlyCollection<BlogResponseDto>>> GetFeedAsync(
        int userId,
        CancellationToken cancellationToken = default)
    {
        if (userId <= 0)
        {
            return ServiceResult<IReadOnlyCollection<BlogResponseDto>>.Failure(
                "User header X-User-Id je obavezan.",
                StatusCodes.Status401Unauthorized);
        }

        IReadOnlyCollection<int> followingIds;
        try
        {
            followingIds = await followerClient.GetFollowingIdsAsync(userId, cancellationToken);
        }
        catch
        {
            return ServiceResult<IReadOnlyCollection<BlogResponseDto>>.Failure(
                "Follower servis trenutno nije dostupan.",
                StatusCodes.Status503ServiceUnavailable);
        }

        var allowedAuthorIds = followingIds
            .Append(userId)
            .Distinct()
            .ToList();

        var blogs = await blogRepository.GetByAuthorIdsAsync(allowedAuthorIds, cancellationToken);
        var results = blogs.Select(blog => MapBlog(blog, userId)).ToList();
        return ServiceResult<IReadOnlyCollection<BlogResponseDto>>.Success(results);
    }

    public async Task<ServiceResult<BlogResponseDto>> GetBlogByIdAsync(
        int blogId,
        int userId,
        CancellationToken cancellationToken = default)
    {
        if (blogId <= 0)
        {
            return ServiceResult<BlogResponseDto>.Failure(
                "Id bloga mora biti pozitivan broj.",
                StatusCodes.Status400BadRequest);
        }

        if (userId <= 0)
        {
            return ServiceResult<BlogResponseDto>.Failure(
                "User header X-User-Id je obavezan.",
                StatusCodes.Status401Unauthorized);
        }

        var blog = await blogRepository.GetByIdAsync(blogId, cancellationToken);
        if (blog is null)
        {
            return ServiceResult<BlogResponseDto>.Failure(
                "Blog nije pronadjen.",
                StatusCodes.Status404NotFound);
        }

        if (!await CanReadAuthorAsync(userId, blog.AuthorId, cancellationToken))
        {
            return ServiceResult<BlogResponseDto>.Failure(
                "Mozete citati samo blogove autora koje pratite.",
                StatusCodes.Status403Forbidden);
        }

        var dto = MapBlog(blog, userId);
        return ServiceResult<BlogResponseDto>.Success(dto);
    }

    public async Task<ServiceResult<BlogResponseDto>> CreateBlogAsync(
        int authorId,
        CreateBlogDto request,
        CancellationToken cancellationToken = default)
    {
        if (authorId <= 0)
        {
            return ServiceResult<BlogResponseDto>.Failure(
                "User header X-User-Id je obavezan.",
                StatusCodes.Status401Unauthorized);
        }

        var title = request.Title.Trim();
        var description = request.Description.Trim();

        if (string.IsNullOrWhiteSpace(title) || string.IsNullOrWhiteSpace(description))
        {
            return ServiceResult<BlogResponseDto>.Failure(
                "Title i Description su obavezni.",
                StatusCodes.Status400BadRequest);
        }

        if (title.Length > MaxTitleLength)
        {
            return ServiceResult<BlogResponseDto>.Failure(
                "Title moze imati najvise 200 karaktera.",
                StatusCodes.Status400BadRequest);
        }

        var imageUrls = new List<string>();
        foreach (var image in request.Images)
        {
            var normalizedImage = image.Trim();
            if (string.IsNullOrWhiteSpace(normalizedImage))
            {
                return ServiceResult<BlogResponseDto>.Failure(
                    "Ako se salju slike, svaka mora imati validan URL ili putanju.",
                    StatusCodes.Status400BadRequest);
            }

            imageUrls.Add(normalizedImage);
        }

        var blog = new Models.Blog
        {
            AuthorId = authorId,
            Title = title,
            Description = description,
            CreatedAtUtc = DateTime.UtcNow,
            Images = imageUrls
                .Select((url, index) => new BlogImage
                {
                    Url = url,
                    OrderIndex = index
                })
                .ToList()
        };

        var savedBlog = await blogRepository.AddAsync(blog, cancellationToken);

        var dto = MapBlog(savedBlog, userId: authorId);
        return ServiceResult<BlogResponseDto>.Success(
            dto,
            StatusCodes.Status201Created,
            "Blog je uspesno kreiran.");
    }

    private static BlogResponseDto MapBlog(Models.Blog blog, int? userId)
    {
        var isLikedByCurrentUser = false;
        if (userId.HasValue && userId.Value > 0)
        {
            isLikedByCurrentUser = blog.Likes.Any(like => like.UserId == userId.Value);
        }

        return new BlogResponseDto
        {
            Id = blog.Id,
            AuthorId = blog.AuthorId,
            Title = blog.Title,
            Description = blog.Description,
            CreatedAtUtc = blog.CreatedAtUtc,
            Images = blog.Images
                .OrderBy(image => image.OrderIndex)
                .Select(image => image.Url)
                .ToList(),
            Comments = blog.Comments
                .OrderByDescending(comment => comment.CreatedAtUtc)
                .Select(comment => new CommentResponseDto
                {
                    Id = comment.Id,
                    BlogId = comment.BlogId,
                    UserId = comment.UserId,
                    Text = comment.Text,
                    CreatedAtUtc = comment.CreatedAtUtc,
                    UpdatedAtUtc = comment.UpdatedAtUtc
                })
                .ToList(),
            LikesCount = blog.Likes.Count,
            IsLikedByCurrentUser = isLikedByCurrentUser
        };
    }

    private async Task<bool> CanReadAuthorAsync(
        int userId,
        int authorId,
        CancellationToken cancellationToken)
    {
        if (userId == authorId)
        {
            return true;
        }

        try
        {
            return await followerClient.IsFollowingAsync(userId, authorId, cancellationToken);
        }
        catch
        {
            return false;
        }
    }
}
