using Microsoft.AspNetCore.Mvc;
using StakeholdersService.DTOs;
using StakeholdersService.Services;

namespace StakeholdersService.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController(IAuthService authService) : ControllerBase
{
    [HttpPost("login")]
    [ProducesResponseType(typeof(LoginResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status403Forbidden)]
    public async Task<IActionResult> Login([FromBody] LoginUserDto request, CancellationToken cancellationToken)
    {
        var result = await authService.LoginAsync(request, cancellationToken);

        if (!result.IsSuccess)
        {
            return StatusCode(result.StatusCode, new ErrorResponseDto
            {
                Message = result.Message
            });
        }

        return StatusCode(result.StatusCode, result.Data);
    }

    [HttpPost("register")]
    [ProducesResponseType(typeof(RegisteredUserDto), StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status409Conflict)]
    public async Task<IActionResult> Register([FromBody] RegisterUserDto request, CancellationToken cancellationToken)
    {
        var result = await authService.RegisterAsync(request, cancellationToken);

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
