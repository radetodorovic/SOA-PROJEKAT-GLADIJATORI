using StakeholdersService.DTOs;

namespace StakeholdersService.Services;

public interface IUserActivityService
{
    Task<ServiceResult<UserActivityResponseDto>> LogTourExecutionStartedAsync(
        int userId,
        string? internalApiKey,
        bool simulateFailure,
        CreateTourExecutionActivityDto request,
        CancellationToken cancellationToken = default);
}
