using Microsoft.AspNetCore.Mvc;
using BlogService.DTOs;
using BlogService.Services;

namespace BlogService.Controllers;

[ApiController]
[Route("api/blogs/{blogId:int}/likes")]
public class LikesController(ILikeService likeService) : ControllerBase
{
    [HttpPost]
    [ProducesResponseType(StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status409Conflict)]
    public async Task<IActionResult> LikeBlog(
        int blogId,
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

        var result = await likeService.LikeBlogAsync(blogId, userId, cancellationToken);

        if (!result.IsSuccess)
        {
            return StatusCode(result.StatusCode, new ErrorResponseDto
            {
                Message = result.Message
            });
        }

        return StatusCode(result.StatusCode, result.Data);
    }

    [HttpDelete]
    [ProducesResponseType(StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status404NotFound)]
    public async Task<IActionResult> UnlikeBlog(
        int blogId,
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

        var result = await likeService.UnlikeBlogAsync(blogId, userId, cancellationToken);

        if (!result.IsSuccess)
        {
            return StatusCode(result.StatusCode, new ErrorResponseDto
            {
                Message = result.Message
            });
        }

        return StatusCode(result.StatusCode, result.Data);
    }

    [HttpGet("count")]
    [ProducesResponseType(typeof(int), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> GetLikesCount(
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

        var result = await likeService.GetLikesCountAsync(blogId, cancellationToken);

        if (!result.IsSuccess)
        {
            return StatusCode(result.StatusCode, new ErrorResponseDto
            {
                Message = result.Message
            });
        }

        return Ok(result.Data);
    }

    [HttpGet("check")]
    [ProducesResponseType(typeof(bool), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status401Unauthorized)]
    public async Task<IActionResult> IsLikedByUser(
        int blogId,
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

        var result = await likeService.IsLikedByUserAsync(blogId, userId, cancellationToken);

        if (!result.IsSuccess)
        {
            return StatusCode(result.StatusCode, new ErrorResponseDto
            {
                Message = result.Message
            });
        }

        return Ok(result.Data);
    }
}
