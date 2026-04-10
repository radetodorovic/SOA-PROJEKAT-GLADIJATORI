using Microsoft.AspNetCore.Mvc;
using BlogService.DTOs;
using BlogService.Services;

namespace BlogService.Controllers;

[ApiController]
[Route("api/blogs/{blogId:int}/comments")]
public class CommentsController(ICommentService commentService) : ControllerBase
{
    [HttpGet]
    [ProducesResponseType(typeof(IReadOnlyCollection<CommentResponseDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> GetCommentsByBlogId(
        int blogId,
        CancellationToken cancellationToken)
    {
        if (blogId <= 0)
        {
            return BadRequest(new ErrorResponseDto
            {
                Message = "Id bloga mora biti pozitivan broj."
            });
        }

        var comments = await commentService.GetCommentsByBlogIdAsync(blogId, cancellationToken);
        return Ok(comments);
    }

    [HttpGet("{commentId:int}")]
    [ProducesResponseType(typeof(CommentResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetCommentById(
        int blogId,
        int commentId,
        CancellationToken cancellationToken)
    {
        if (blogId <= 0)
        {
            return BadRequest(new ErrorResponseDto
            {
                Message = "Id bloga mora biti pozitivan broj."
            });
        }

        var result = await commentService.GetCommentByIdAsync(commentId, cancellationToken);

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
    [ProducesResponseType(typeof(CommentResponseDto), StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status404NotFound)]
    public async Task<IActionResult> CreateComment(
        int blogId,
        [FromHeader(Name = "X-User-Id")] int userId,
        [FromBody] CreateCommentDto request,
        CancellationToken cancellationToken)
    {
        if (blogId <= 0)
        {
            return BadRequest(new ErrorResponseDto
            {
                Message = "Id bloga mora biti pozitivan broj."
            });
        }

        var result = await commentService.CreateCommentAsync(blogId, userId, request, cancellationToken);

        if (!result.IsSuccess)
        {
            return StatusCode(result.StatusCode, new ErrorResponseDto
            {
                Message = result.Message
            });
        }

        return StatusCode(result.StatusCode, result.Data);
    }

    [HttpPut("{commentId:int}")]
    [ProducesResponseType(typeof(CommentResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status403Forbidden)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status404NotFound)]
    public async Task<IActionResult> UpdateComment(
        int blogId,
        int commentId,
        [FromHeader(Name = "X-User-Id")] int userId,
        [FromBody] CreateCommentDto request,
        CancellationToken cancellationToken)
    {
        if (blogId <= 0)
        {
            return BadRequest(new ErrorResponseDto
            {
                Message = "Id bloga mora biti pozitivan broj."
            });
        }

        var result = await commentService.UpdateCommentAsync(commentId, userId, request, cancellationToken);

        if (!result.IsSuccess)
        {
            return StatusCode(result.StatusCode, new ErrorResponseDto
            {
                Message = result.Message
            });
        }

        return StatusCode(result.StatusCode, result.Data);
    }

    [HttpDelete("{commentId:int}")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status403Forbidden)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status404NotFound)]
    public async Task<IActionResult> DeleteComment(
        int blogId,
        int commentId,
        [FromHeader(Name = "X-User-Id")] int userId,
        CancellationToken cancellationToken)
    {
        if (blogId <= 0)
        {
            return BadRequest(new ErrorResponseDto
            {
                Message = "Id bloga mora biti pozitivan broj."
            });
        }

        var result = await commentService.DeleteCommentAsync(commentId, userId, cancellationToken);

        if (!result.IsSuccess)
        {
            return StatusCode(result.StatusCode, new ErrorResponseDto
            {
                Message = result.Message
            });
        }

        return StatusCode(result.StatusCode);
    }
}
