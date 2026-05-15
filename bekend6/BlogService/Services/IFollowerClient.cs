namespace BlogService.Services;

public interface IFollowerClient
{
    Task<IReadOnlyCollection<int>> GetFollowingIdsAsync(int userId, CancellationToken cancellationToken = default);
    Task<bool> IsFollowingAsync(int followerId, int targetId, CancellationToken cancellationToken = default);
}
