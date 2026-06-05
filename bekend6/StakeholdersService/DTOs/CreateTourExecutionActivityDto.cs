namespace StakeholdersService.DTOs;

public class CreateTourExecutionActivityDto
{
    public string TourId { get; init; } = string.Empty;
    public string ExecutionId { get; init; } = string.Empty;
    public string Description { get; init; } = string.Empty;
}
