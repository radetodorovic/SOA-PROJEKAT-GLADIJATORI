using BlogService.DTOs;
using BlogService.Models;
using BlogService.Repositories;

namespace BlogService.Services;

public class BlogService(
    IBlogRepository blogRepository,
    IBlogLikeRepository likeRepository) : IBlogService
{
    private const int MaxTitleLength = 200;

    public async Task<IReadOnlyCollection<BlogResponseDto>> GetAllBlogsAsync(CancellationToken cancellationToken = default)
    {
        var blogs = await blogRepository.GetAllAsync(cancellationToken);
        var tasks = blogs.Select(blog => MapBlog(blog, userId: null));
        var results = await Task.WhenAll(tasks);
        return results.ToList();
    }

    public async Task<ServiceResult<BlogResponseDto>> GetBlogByIdAsync(int blogId, CancellationToken cancellationToken = default)
    {
        if (blogId <= 0)
        {
            return ServiceResult<BlogResponseDto>.Failure(
                "Id bloga mora biti pozitivan broj.",
                StatusCodes.Status400BadRequest);
        }

        var blog = await blogRepository.GetByIdAsync(blogId, cancellationToken);
        if (blog is null)
        {
            return ServiceResult<BlogResponseDto>.Failure(
                "Blog nije pronadjen.",
                StatusCodes.Status404NotFound);
        }

        var dto = await MapBlog(blog, userId: null);
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

        var dto = await MapBlog(savedBlog, userId: authorId);
        return ServiceResult<BlogResponseDto>.Success(
            dto,
            StatusCodes.Status201Created,
            "Blog je uspesno kreiran.");
    }

    private async Task<BlogResponseDto> MapBlog(Models.Blog blog, int? userId)
    {
        var isLikedByCurrentUser = false;
        if (userId.HasValue && userId.Value > 0)
        {
            var like = await likeRepository.GetLikeAsync(blog.Id, userId.Value);
            isLikedByCurrentUser = like is not null;
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
}
