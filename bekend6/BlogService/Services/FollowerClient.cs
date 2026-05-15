using System.Net.Http.Json;
using System.Text.Json;

namespace BlogService.Services;

public class FollowerClient(HttpClient httpClient) : IFollowerClient
{
    public async Task<IReadOnlyCollection<int>> GetFollowingIdsAsync(
        int userId,
        CancellationToken cancellationToken = default)
    {
        using var response = await httpClient.GetAsync($"/api/followers/following/{userId}", cancellationToken);
        response.EnsureSuccessStatusCode();

        var payload = await response.Content.ReadFromJsonAsync<FollowingResponse>(
            cancellationToken: cancellationToken);

        return payload?.Users.Select(user => user.Id).ToList() ?? [];
    }

    public async Task<bool> IsFollowingAsync(
        int followerId,
        int targetId,
        CancellationToken cancellationToken = default)
    {
        using var response = await httpClient.GetAsync(
            $"/api/followers/check?followerId={followerId}&targetId={targetId}",
            cancellationToken);
        response.EnsureSuccessStatusCode();

        var payload = await response.Content.ReadFromJsonAsync<CheckResponse>(
            new JsonSerializerOptions { PropertyNameCaseInsensitive = true },
            cancellationToken);

        return payload?.IsFollowing ?? false;
    }

    private sealed class FollowingResponse
    {
        public List<FollowerUser> Users { get; init; } = [];
    }

    private sealed class FollowerUser
    {
        public int Id { get; init; }
    }

    private sealed class CheckResponse
    {
        public bool IsFollowing { get; init; }
    }
}
