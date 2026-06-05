using Microsoft.EntityFrameworkCore;
using StakeholdersService.Data;
using StakeholdersService.DTOs;
using StakeholdersService.Models;
using StakeholdersService.Repositories;

namespace StakeholdersService.Services;

public class UserActivityService(
    AppDbContext dbContext,
    IUserRepository userRepository,
    IConfiguration configuration,
    ILogger<UserActivityService> logger) : IUserActivityService
{
    private const string TourExecutionStarted = "TOUR_EXECUTION_STARTED";
    private readonly string internalApiKey = configuration["InternalApiKey"] ?? "stakeholders-service-internal-dev-key";

    public async Task<ServiceResult<UserActivityResponseDto>> LogTourExecutionStartedAsync(
        int userId,
        string? providedInternalApiKey,
        bool simulateFailure,
        CreateTourExecutionActivityDto request,
        CancellationToken cancellationToken = default)
    {
        if (!string.Equals(providedInternalApiKey, internalApiKey, StringComparison.Ordinal))
        {
            return ServiceResult<UserActivityResponseDto>.Failure(
                "Ruta nije pronadjena.",
                StatusCodes.Status404NotFound);
        }

        var user = await userRepository.GetByIdAsync(userId, cancellationToken);
        if (user is null)
        {
            return ServiceResult<UserActivityResponseDto>.Failure(
                "Korisnik nije pronadjen.",
                StatusCodes.Status404NotFound);
        }

        if (user.Role != UserRole.Tourist)
        {
            return ServiceResult<UserActivityResponseDto>.Failure(
                "Aktivnost se moze upisati samo za turista.",
                StatusCodes.Status403Forbidden);
        }

        if (string.IsNullOrWhiteSpace(request.TourId) || string.IsNullOrWhiteSpace(request.ExecutionId))
        {
            return ServiceResult<UserActivityResponseDto>.Failure(
                "TourId i ExecutionId su obavezni.",
                StatusCodes.Status400BadRequest);
        }

        if (simulateFailure)
        {
            logger.LogWarning(
                "StartTourExecutionSaga activity log simulation failed. userId={UserId}, tourId={TourId}, executionId={ExecutionId}",
                userId,
                request.TourId,
                request.ExecutionId);

            return ServiceResult<UserActivityResponseDto>.Failure(
                "Simulirani upis aktivnosti nije uspeo.",
                StatusCodes.Status502BadGateway);
        }

        var activity = new UserActivityLog
        {
            UserId = userId,
            ActivityType = TourExecutionStarted,
            ReferenceId = request.ExecutionId,
            Description = string.IsNullOrWhiteSpace(request.Description)
                ? $"Tour execution started for tour {request.TourId}."
                : request.Description.Trim(),
            CreatedAt = DateTime.UtcNow
        };

        dbContext.Set<UserActivityLog>().Add(activity);
        await dbContext.SaveChangesAsync(cancellationToken);

        logger.LogInformation(
            "StartTourExecutionSaga activity log stored. userId={UserId}, tourId={TourId}, executionId={ExecutionId}, activityId={ActivityId}",
            userId,
            request.TourId,
            request.ExecutionId,
            activity.Id);

        return ServiceResult<UserActivityResponseDto>.Success(
            new UserActivityResponseDto
            {
                Id = activity.Id,
                UserId = activity.UserId,
                ActivityType = activity.ActivityType,
                ReferenceId = activity.ReferenceId,
                Description = activity.Description,
                CreatedAt = activity.CreatedAt
            },
            StatusCodes.Status201Created,
            "Aktivnost pokretanja ture je upisana.");
    }
}
