using System.Net.Http.Headers;
using System.Net.Http.Json;
using ApiGateway.Grpc;
using Grpc.Core;
using Grpc.Net.Client;

var builder = WebApplication.CreateBuilder(args);

AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);

builder.Services.AddHttpClient("gateway-proxy", client =>
{
    client.Timeout = TimeSpan.FromSeconds(100);
});

builder.Services.AddCors(options =>
{
    options.AddPolicy("Frontend", policy =>
    {
        policy
            .WithOrigins(
                "http://localhost:4200",
                "https://localhost:4200",
                "http://127.0.0.1:4200",
                "https://127.0.0.1:4200")
            .AllowAnyHeader()
            .AllowAnyMethod();
    });
});

var app = builder.Build();

app.UseCors("Frontend");

var stakeholdersServiceBaseUrl = GetServiceBaseUrl(builder.Configuration, "StakeholdersService", "http://localhost:5008");
var blogServiceBaseUrl = GetServiceBaseUrl(builder.Configuration, "BlogService", "http://localhost:5009");
var followerServiceBaseUrl = GetServiceBaseUrl(builder.Configuration, "FollowerService", "http://localhost:5010");
var tourServiceBaseUrl = GetServiceBaseUrl(builder.Configuration, "TourService", "http://localhost:5011");

var routes = new[]
{
    new GatewayRoute("/api/auth", stakeholdersServiceBaseUrl),
    new GatewayRoute("/api/users", stakeholdersServiceBaseUrl),
    new GatewayRoute("/api/blogs", blogServiceBaseUrl),
    new GatewayRoute("/api/followers", followerServiceBaseUrl),
    new GatewayRoute("/api/tours", tourServiceBaseUrl)
};

var tourGrpcUrl = GetServiceConfigValue(builder.Configuration, "TourService", "GrpcUrl", "http://localhost:5012");
var tourGrpcChannel = GrpcChannel.ForAddress(tourGrpcUrl);
var tourGrpcClient = new TourRpc.TourRpcClient(tourGrpcChannel);

app.MapGet("/health", () => Results.Ok(new
{
    status = "ok",
    service = "api-gateway"
}));

app.MapGet("/", () => Results.Ok(CreateGatewayInfo(routes)));
app.MapGet("/api", () => Results.Ok(CreateGatewayInfo(routes)));

app.MapGet("/api/tours/published", async () =>
{
    var reply = await tourGrpcClient.GetPublishedToursAsync(new GetPublishedToursRequest());
    return Results.Ok(reply.Tours.Select(MapTourReply));
});

app.MapGet("/api/tours/{id:regex(^[0-9a-fA-F]{{24}}$)}", async (string id, HttpContext context) =>
{
    try
    {
        var reply = await tourGrpcClient.GetTourByIdAsync(new GetTourByIdRequest
        {
            Id = id,
            UserId = GetUserId(context)
        });
        return Results.Ok(MapTourReply(reply));
    }
    catch (RpcException ex)
    {
        return Results.Json(new { message = ex.Status.Detail }, statusCode: MapGrpcStatusCode(ex.StatusCode));
    }
});

app.MapPost("/api/sagas/checkout", async (
    HttpContext context,
    IHttpClientFactory httpClientFactory) =>
{
    var userId = GetUserId(context);
    var httpClient = httpClientFactory.CreateClient("gateway-proxy");
    var steps = new List<SagaStepResult>();

    var userValidation = await ValidateTourist(userId, httpClient, stakeholdersServiceBaseUrl, context.RequestAborted);
    steps.Add(new SagaStepResult("Validate tourist in StakeholdersService", userValidation.Success, userValidation.Message));
    if (!userValidation.Success)
    {
        return Results.Json(CreateSagaResponse("CheckoutSaga", "FAILED", steps, null), statusCode: userValidation.StatusCode);
    }

    using var checkoutRequest = new HttpRequestMessage(HttpMethod.Post, new Uri($"{tourServiceBaseUrl}/api/tours/cart/checkout"));
    checkoutRequest.Headers.TryAddWithoutValidation("X-User-Id", userId.ToString());
    using var checkoutResponse = await httpClient.SendAsync(checkoutRequest, context.RequestAborted);
    var checkoutJson = await checkoutResponse.Content.ReadAsStringAsync(context.RequestAborted);

    steps.Add(new SagaStepResult("Checkout cart in TourService", checkoutResponse.IsSuccessStatusCode, checkoutResponse.IsSuccessStatusCode
        ? "TourPurchaseToken-i su generisani i korpa je ispraznjena."
        : checkoutJson));

    if (!checkoutResponse.IsSuccessStatusCode)
    {
        steps.Add(new SagaStepResult("Compensation", true, "Nije potrebna: checkout nije uspeo, tokeni nisu potvrđeni."));
        return Results.Json(CreateSagaResponse("CheckoutSaga", "FAILED", steps, checkoutJson), statusCode: (int)checkoutResponse.StatusCode);
    }

    return Results.Json(CreateSagaResponse("CheckoutSaga", "COMPLETED", steps, checkoutJson));
});

app.MapPost("/api/sagas/tours/{tourId}/execution/start", async (
    string tourId,
    HttpContext context,
    IHttpClientFactory httpClientFactory) =>
{
    var userId = GetUserId(context);
    var httpClient = httpClientFactory.CreateClient("gateway-proxy");
    var steps = new List<SagaStepResult>();

    var userValidation = await ValidateTourist(userId, httpClient, stakeholdersServiceBaseUrl, context.RequestAborted);
    steps.Add(new SagaStepResult("Validate tourist in StakeholdersService", userValidation.Success, userValidation.Message));
    if (!userValidation.Success)
    {
        return Results.Json(CreateSagaResponse("StartTourExecutionSaga", "FAILED", steps, null), statusCode: userValidation.StatusCode);
    }

    using var startRequest = new HttpRequestMessage(HttpMethod.Post, new Uri($"{tourServiceBaseUrl}/api/tours/{tourId}/execution/start"));
    startRequest.Headers.TryAddWithoutValidation("X-User-Id", userId.ToString());
    using var startResponse = await httpClient.SendAsync(startRequest, context.RequestAborted);
    var startJson = await startResponse.Content.ReadAsStringAsync(context.RequestAborted);

    steps.Add(new SagaStepResult("Start TourExecution in TourService", startResponse.IsSuccessStatusCode, startResponse.IsSuccessStatusCode
        ? "Aktivna sesija ture je kreirana ili vracena."
        : startJson));

    if (!startResponse.IsSuccessStatusCode)
    {
        steps.Add(new SagaStepResult("Compensation", true, "Nije potrebna: TourExecution nije pokrenut."));
        return Results.Json(CreateSagaResponse("StartTourExecutionSaga", "FAILED", steps, startJson), statusCode: (int)startResponse.StatusCode);
    }

    return Results.Json(CreateSagaResponse("StartTourExecutionSaga", "COMPLETED", steps, startJson));
});

app.Map("/{**path}", async (
    HttpContext context,
    IHttpClientFactory httpClientFactory) =>
{
    var path = context.Request.Path.Value ?? string.Empty;
    var route = routes
        .Where(candidate => IsRouteMatch(path, candidate.PathPrefix))
        .OrderByDescending(candidate => candidate.PathPrefix.Length)
        .FirstOrDefault();

    if (route is null)
    {
        context.Response.StatusCode = StatusCodes.Status404NotFound;
        await context.Response.WriteAsJsonAsync(new
        {
            message = "Gateway route nije pronadjena."
        });
        return;
    }

    await ProxyRequest(context, httpClientFactory.CreateClient("gateway-proxy"), route.BaseUri);
});

app.Run();

static string GetServiceBaseUrl(IConfiguration configuration, string serviceName, string fallback)
{
    return GetServiceConfigValue(configuration, serviceName, "BaseUrl", fallback);
}

static string GetServiceConfigValue(IConfiguration configuration, string serviceName, string optionName, string fallback)
{
    var key = $"Services:{serviceName}:{optionName}";
    var configured = configuration[key];
    return string.IsNullOrWhiteSpace(configured) ? fallback : configured;
}

static int GetUserId(HttpContext context)
{
    return int.TryParse(context.Request.Headers["X-User-Id"].FirstOrDefault(), out var userId) ? userId : 0;
}

static int MapGrpcStatusCode(StatusCode statusCode)
{
    return statusCode switch
    {
        StatusCode.NotFound => StatusCodes.Status404NotFound,
        StatusCode.InvalidArgument => StatusCodes.Status400BadRequest,
        StatusCode.Unauthenticated => StatusCodes.Status401Unauthorized,
        StatusCode.PermissionDenied => StatusCodes.Status403Forbidden,
        _ => StatusCodes.Status500InternalServerError
    };
}

static object MapTourReply(TourReply tour)
{
    return new
    {
        id = tour.Id,
        authorId = tour.AuthorId,
        name = tour.Name,
        description = tour.Description,
        status = tour.Status,
        difficulty = tour.Difficulty,
        price = tour.Price,
        distanceKm = tour.DistanceKm,
        transportDurations = tour.TransportDurations.ToDictionary(entry => entry.Key, entry => entry.Value),
        keyPoints = tour.KeyPoints.Select(MapKeyPointReply).ToArray(),
        tags = tour.Tags.ToArray(),
        createdAt = EmptyToNull(tour.CreatedAt),
        publishedAt = EmptyToNull(tour.PublishedAt),
        archivedAt = EmptyToNull(tour.ArchivedAt)
    };
}

static object MapKeyPointReply(KeyPointReply keyPoint)
{
    return new
    {
        id = keyPoint.Id,
        name = keyPoint.Name,
        description = EmptyToNull(keyPoint.Description),
        latitude = keyPoint.Latitude,
        longitude = keyPoint.Longitude,
        imageUrl = EmptyToNull(keyPoint.ImageUrl),
        order = keyPoint.Order
    };
}

static string? EmptyToNull(string value)
{
    return string.IsNullOrWhiteSpace(value) ? null : value;
}

static async Task<UserValidationResult> ValidateTourist(
    int userId,
    HttpClient httpClient,
    string stakeholdersServiceBaseUrl,
    CancellationToken cancellationToken)
{
    if (userId <= 0)
    {
        return new UserValidationResult(false, StatusCodes.Status401Unauthorized, "X-User-Id header je obavezan.");
    }

    var users = await httpClient.GetFromJsonAsync<List<UserSummary>>(
        new Uri($"{stakeholdersServiceBaseUrl}/api/users"),
        cancellationToken);
    var user = users?.FirstOrDefault(candidate => candidate.Id == userId);
    if (user is null)
    {
        return new UserValidationResult(false, StatusCodes.Status404NotFound, "Korisnik nije pronadjen u StakeholdersService.");
    }
    if (user.IsBlocked)
    {
        return new UserValidationResult(false, StatusCodes.Status403Forbidden, "Blokiran korisnik ne moze izvrsiti SAGA tok.");
    }
    if (!string.Equals(user.Role, "Tourist", StringComparison.OrdinalIgnoreCase))
    {
        return new UserValidationResult(false, StatusCodes.Status403Forbidden, "SAGA tok je dozvoljen samo turistima.");
    }

    return new UserValidationResult(true, StatusCodes.Status200OK, $"Turista {user.Username} je validiran.");
}

static object CreateSagaResponse(string saga, string status, List<SagaStepResult> steps, string? payload)
{
    return new
    {
        saga,
        status,
        steps,
        payload = TryParseJson(payload)
    };
}

static object? TryParseJson(string? payload)
{
    if (string.IsNullOrWhiteSpace(payload))
    {
        return null;
    }

    try
    {
        return System.Text.Json.JsonSerializer.Deserialize<object>(payload);
    }
    catch
    {
        return payload;
    }
}

static bool IsRouteMatch(string path, string pathPrefix)
{
    return path.Equals(pathPrefix, StringComparison.OrdinalIgnoreCase) ||
        path.StartsWith($"{pathPrefix}/", StringComparison.OrdinalIgnoreCase);
}

static object CreateGatewayInfo(IEnumerable<GatewayRoute> routes)
{
    return new
    {
        status = "ok",
        service = "api-gateway",
        routes = routes.Select(route => route.PathPrefix).ToArray()
    };
}

static async Task ProxyRequest(HttpContext context, HttpClient httpClient, Uri targetBaseUri)
{
    using var proxyRequest = CreateProxyRequest(context, targetBaseUri);
    using var proxyResponse = await httpClient.SendAsync(
        proxyRequest,
        HttpCompletionOption.ResponseHeadersRead,
        context.RequestAborted);

    context.Response.StatusCode = (int)proxyResponse.StatusCode;

    CopyHeaders(proxyResponse.Headers, context.Response.Headers);
    CopyHeaders(proxyResponse.Content.Headers, context.Response.Headers);
    context.Response.Headers.Remove("transfer-encoding");

    await proxyResponse.Content.CopyToAsync(context.Response.Body, context.RequestAborted);
}

static HttpRequestMessage CreateProxyRequest(HttpContext context, Uri targetBaseUri)
{
    var request = context.Request;
    var targetUri = new UriBuilder(targetBaseUri)
    {
        Path = request.Path.Value,
        Query = request.QueryString.HasValue
            ? request.QueryString.Value![1..]
            : string.Empty
    }.Uri;

    var proxyRequest = new HttpRequestMessage(new HttpMethod(request.Method), targetUri);

    if (ShouldProxyBody(request))
    {
        proxyRequest.Content = new StreamContent(request.Body);
        foreach (var header in request.Headers)
        {
            if (!proxyRequest.Headers.TryAddWithoutValidation(header.Key, header.Value.ToArray()))
            {
                proxyRequest.Content.Headers.TryAddWithoutValidation(header.Key, header.Value.ToArray());
            }
        }
    }
    else
    {
        foreach (var header in request.Headers)
        {
            proxyRequest.Headers.TryAddWithoutValidation(header.Key, header.Value.ToArray());
        }
    }

    proxyRequest.Headers.Host = null;
    proxyRequest.Headers.AcceptEncoding.Clear();
    return proxyRequest;
}

static bool ShouldProxyBody(HttpRequest request)
{
    return request.ContentLength > 0 ||
        string.Equals(request.Method, HttpMethods.Post, StringComparison.OrdinalIgnoreCase) ||
        string.Equals(request.Method, HttpMethods.Put, StringComparison.OrdinalIgnoreCase) ||
        string.Equals(request.Method, HttpMethods.Patch, StringComparison.OrdinalIgnoreCase);
}

static void CopyHeaders(HttpHeaders source, IHeaderDictionary destination)
{
    foreach (var header in source)
    {
        destination[header.Key] = header.Value.ToArray();
    }
}

sealed record GatewayRoute(string PathPrefix, Uri BaseUri)
{
    public GatewayRoute(string pathPrefix, string baseUrl)
        : this(pathPrefix, new Uri(baseUrl.TrimEnd('/')))
    {
    }
}

sealed record UserSummary(int Id, string Username, string Email, string Role, bool IsBlocked);

sealed record UserValidationResult(bool Success, int StatusCode, string Message);

sealed record SagaStepResult(string Step, bool Success, string Message);
