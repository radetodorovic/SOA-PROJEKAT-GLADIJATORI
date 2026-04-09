using BlogService.DTOs;
using BlogService.Models;
using BlogService.Repositories;

namespace BlogService.Services;

public class BlogService(IBlogRepository blogRepository) : IBlogService
{
    private const int MaxTitleLength = 200;

    public async Task<IReadOnlyCollection<BlogResponseDto>> GetAllBlogsAsync(CancellationToken cancellationToken = default)
    {
        var blogs = await blogRepository.GetAllAsync(cancellationToken);
        return blogs.Select(MapBlog).ToList();
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

        return ServiceResult<BlogResponseDto>.Success(MapBlog(blog));
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

        return ServiceResult<BlogResponseDto>.Success(
            MapBlog(savedBlog),
            StatusCodes.Status201Created,
            "Blog je uspesno kreiran.");
    }

    private static BlogResponseDto MapBlog(Models.Blog blog)
    {
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
                .ToList()
        };
    }
}
