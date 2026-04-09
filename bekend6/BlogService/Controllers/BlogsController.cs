using Microsoft.AspNetCore.Mvc;
using BlogService.DTOs;
using BlogService.Services;

namespace BlogService.Controllers;

[ApiController]
[Route("api/[controller]")]
public class BlogsController(IBlogService blogService) : ControllerBase
{
    [HttpGet]
    [ProducesResponseType(typeof(IReadOnlyCollection<BlogResponseDto>), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetAllBlogs(CancellationToken cancellationToken)
    {
        var blogs = await blogService.GetAllBlogsAsync(cancellationToken);
        return Ok(blogs);
    }

    [HttpGet("{id:int}")]
    [ProducesResponseType(typeof(BlogResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetBlogById(int id, CancellationToken cancellationToken)
    {
        var result = await blogService.GetBlogByIdAsync(id, cancellationToken);

        if (!result.IsSuccess)
        {
            return StatusCode(result.StatusCode, new ErrorResponseDto
            {
                Message = result.Message
            });
        }

        return Ok(result.Data);
    }

    [HttpPost]
    [ProducesResponseType(typeof(BlogResponseDto), StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status401Unauthorized)]
    public async Task<IActionResult> CreateBlog(
        [FromHeader(Name = "X-User-Id")] int userId,
        [FromBody] CreateBlogDto request,
        CancellationToken cancellationToken)
    {
        var result = await blogService.CreateBlogAsync(userId, request, cancellationToken);

        if (!result.IsSuccess)
        {
            return StatusCode(result.StatusCode, new ErrorResponseDto
            {
                Message = result.Message
            });
        }

        return StatusCode(result.StatusCode, result.Data);
    }
}
