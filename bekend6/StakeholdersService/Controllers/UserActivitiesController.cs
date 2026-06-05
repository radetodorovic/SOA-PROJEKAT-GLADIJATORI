using Microsoft.AspNetCore.Mvc;
using StakeholdersService.DTOs;
using StakeholdersService.Services;

namespace StakeholdersService.Controllers;

[ApiController]
[Route("api/users/{id:int}/activities")]
public class UserActivitiesController(IUserActivityService userActivityService) : ControllerBase
{
    [HttpPost("tour-execution-started")]
    [ProducesResponseType(typeof(UserActivityResponseDto), StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status403Forbidden)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(ErrorResponseDto), StatusCodes.Status502BadGateway)]
    public async Task<IActionResult> LogTourExecutionStarted(
        int id,
        [FromHeader(Name = "X-Internal-Api-Key")] string? internalApiKey,
        [FromHeader(Name = "X-Simulate-Execution-Activity-Failure")] string? simulateFailureHeader,
        [FromBody] CreateTourExecutionActivityDto request,
        CancellationToken cancellationToken)
    {
        var simulateFailure = string.Equals(simulateFailureHeader, "true", StringComparison.OrdinalIgnoreCase);
        var result = await userActivityService.LogTourExecutionStartedAsync(
            id,
            internalApiKey,
            simulateFailure,
            request,
            cancellationToken);

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
