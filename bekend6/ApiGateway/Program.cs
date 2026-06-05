using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using System.Text.RegularExpressions;
using ApiGateway.Grpc;
using Grpc.Core;
using Grpc.Net.Client;

const string TourServiceInternalApiKeyHeader = "X-Internal-Api-Key";
const string SimulatePaymentFailureHeader = "X-Simulate-Payment-Failure";
const string SimulateExecutionActivityFailureHeader = "X-Simulate-Execution-Activity-Failure";

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
var stakeholdersServiceInternalApiKey = GetServiceConfigValue(builder.Configuration, "StakeholdersService", "InternalApiKey", "stakeholders-service-internal-dev-key");
var tourServiceInternalApiKey = GetServiceConfigValue(builder.Configuration, "TourService", "InternalApiKey", "tour-service-internal-dev-key");

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

app.MapGet("/api/tours/purchased", async (
    HttpContext context,
    IHttpClientFactory httpClientFactory) =>
{
    var userId = GetUserId(context);
    var httpClient = httpClientFactory.CreateClient("gateway-proxy");

    var userValidation = await ValidateTourist(userId, httpClient, stakeholdersServiceBaseUrl, context.RequestAborted);
    if (!userValidation.Success)
    {
        return Results.Json(new { message = userValidation.Message }, statusCode: userValidation.StatusCode);
    }

    using var request = new HttpRequestMessage(HttpMethod.Get, new Uri($"{tourServiceBaseUrl}/api/tours/purchased"));
    request.Headers.TryAddWithoutValidation("X-User-Id", userId.ToString());
    request.Headers.TryAddWithoutValidation(TourServiceInternalApiKeyHeader, tourServiceInternalApiKey);
    using var response = await httpClient.SendAsync(request, context.RequestAborted);
    var json = await response.Content.ReadAsStringAsync(context.RequestAborted);
    return Results.Json(TryParseJson(json), statusCode: (int)response.StatusCode);
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

app.MapPost("/api/tours", async (CreateTourGatewayRequest request, HttpContext context) =>
{
    try
    {
        var reply = await tourGrpcClient.CreateTourAsync(
            BuildCreateTourRequest(GetUserId(context), request),
            cancellationToken: context.RequestAborted);
        return Results.Json(MapTourReply(reply), statusCode: StatusCodes.Status201Created);
    }
    catch (RpcException ex)
    {
        return GrpcFailure(ex);
    }
});

app.MapPut("/api/tours/{id:regex(^[0-9a-fA-F]{{24}}$)}", async (
    string id,
    UpdateTourGatewayRequest request,
    HttpContext context) =>
{
    try
    {
        var reply = await tourGrpcClient.UpdateTourAsync(
            BuildUpdateTourRequest(id, GetUserId(context), request),
            cancellationToken: context.RequestAborted);
        return Results.Ok(MapTourReply(reply));
    }
    catch (RpcException ex)
    {
        return GrpcFailure(ex);
    }
});

app.MapPost("/api/tours/{tourId:regex(^[0-9a-fA-F]{{24}}$)}/keypoints", async (
    string tourId,
    CreateKeyPointGatewayRequest request,
    HttpContext context) =>
{
    try
    {
        var reply = await tourGrpcClient.AddKeyPointAsync(
            BuildAddKeyPointRequest(tourId, GetUserId(context), request),
            cancellationToken: context.RequestAborted);
        return Results.Json(MapTourReply(reply), statusCode: StatusCodes.Status201Created);
    }
    catch (RpcException ex)
    {
        return GrpcFailure(ex);
    }
});

app.MapPost("/api/tours/cart/items/{tourId:regex(^[0-9a-fA-F]{{24}}$)}", async (
    string tourId,
    HttpContext context) =>
{
    try
    {
        var reply = await tourGrpcClient.AddToCartAsync(
            new AddToCartRequest
            {
                UserId = GetUserId(context),
                TourId = tourId
            },
            cancellationToken: context.RequestAborted);
        return Results.Json(MapShoppingCartReply(reply), statusCode: StatusCodes.Status201Created);
    }
    catch (RpcException ex)
    {
        return GrpcFailure(ex);
    }
});

app.MapPost("/api/tours/{tourId:regex(^[0-9a-fA-F]{{24}}$)}/execution/check", async (
    string tourId,
    HttpContext context) =>
{
    try
    {
        var reply = await tourGrpcClient.CheckExecutionProgressAsync(
            new TourExecutionActionRequest
            {
                TourId = tourId,
                UserId = GetUserId(context)
            },
            cancellationToken: context.RequestAborted);
        return Results.Ok(MapExecutionReply(reply));
    }
    catch (RpcException ex)
    {
        return GrpcFailure(ex);
    }
});

app.MapPost("/api/tours/{tourId:regex(^[0-9a-fA-F]{{24}}$)}/execution/complete", async (
    string tourId,
    HttpContext context) =>
{
    try
    {
        var reply = await tourGrpcClient.CompleteExecutionAsync(
            new TourExecutionActionRequest
            {
                TourId = tourId,
                UserId = GetUserId(context)
            },
            cancellationToken: context.RequestAborted);
        return Results.Ok(MapExecutionReply(reply));
    }
    catch (RpcException ex)
    {
        return GrpcFailure(ex);
    }
});

app.MapPost("/api/tours/{tourId:regex(^[0-9a-fA-F]{{24}}$)}/execution/abandon", async (
    string tourId,
    HttpContext context) =>
{
    try
    {
        var reply = await tourGrpcClient.AbandonExecutionAsync(
            new TourExecutionActionRequest
            {
                TourId = tourId,
                UserId = GetUserId(context)
            },
            cancellationToken: context.RequestAborted);
        return Results.Ok(MapExecutionReply(reply));
    }
    catch (RpcException ex)
    {
        return GrpcFailure(ex);
    }
});

// Direct gRPC aliases keep KT3 coverage while leaving saga-backed public routes intact.
app.MapPost("/api/grpc/tours/cart/checkout", async (
    HttpContext context,
    IHttpClientFactory httpClientFactory) =>
{
    var userId = GetUserId(context);
    var httpClient = httpClientFactory.CreateClient("gateway-proxy");
    var userValidation = await ValidateTourist(userId, httpClient, stakeholdersServiceBaseUrl, context.RequestAborted);
    if (!userValidation.Success)
    {
        return Results.Json(new { message = userValidation.Message }, statusCode: userValidation.StatusCode);
    }

    try
    {
        var reply = await tourGrpcClient.CheckoutAsync(
            new CheckoutRequest { UserId = userId },
            cancellationToken: context.RequestAborted);
        return Results.Ok(MapPurchaseTokenListReply(reply));
    }
    catch (RpcException ex)
    {
        return GrpcFailure(ex);
    }
});

app.MapPost("/api/grpc/tours/{tourId:regex(^[0-9a-fA-F]{{24}}$)}/execution/start", async (
    string tourId,
    HttpContext context,
    IHttpClientFactory httpClientFactory) =>
{
    var userId = GetUserId(context);
    var httpClient = httpClientFactory.CreateClient("gateway-proxy");
    var userValidation = await ValidateTourist(userId, httpClient, stakeholdersServiceBaseUrl, context.RequestAborted);
    if (!userValidation.Success)
    {
        return Results.Json(new { message = userValidation.Message }, statusCode: userValidation.StatusCode);
    }

    try
    {
        var reply = await tourGrpcClient.StartTourExecutionAsync(
            new TourExecutionActionRequest
            {
                TourId = tourId,
                UserId = userId
            },
            cancellationToken: context.RequestAborted);
        return Results.Json(MapExecutionReply(reply), statusCode: StatusCodes.Status201Created);
    }
    catch (RpcException ex)
    {
        return GrpcFailure(ex);
    }
});

app.MapPost("/api/sagas/checkout", async (
    HttpContext context,
    IHttpClientFactory httpClientFactory) =>
{
    var userId = GetUserId(context);
    var httpClient = httpClientFactory.CreateClient("gateway-proxy");
    var steps = new List<SagaStepResult>();
    string? checkoutId = null;

    var userValidation = await ValidateTourist(userId, httpClient, stakeholdersServiceBaseUrl, context.RequestAborted);
    steps.Add(new SagaStepResult("Validate tourist in StakeholdersService", userValidation.Success, userValidation.Message));
    if (!userValidation.Success)
    {
        return Results.Json(CreateSagaResponse("CheckoutSaga", "FAILED", steps, null), statusCode: userValidation.StatusCode);
    }

    using (var validateRequest = CreateInternalTourServiceRequest(
               HttpMethod.Post,
               $"{tourServiceBaseUrl}/api/tours/cart/checkout/validate",
               userId,
               tourServiceInternalApiKey))
    using (var validateResponse = await httpClient.SendAsync(validateRequest, context.RequestAborted))
    {
        var validateJson = await validateResponse.Content.ReadAsStringAsync(context.RequestAborted);
        steps.Add(new SagaStepResult("Validate cart and tours in TourService", validateResponse.IsSuccessStatusCode, validateResponse.IsSuccessStatusCode
            ? "Korpa i ture su validirane za checkout."
            : validateJson));

        if (!validateResponse.IsSuccessStatusCode)
        {
            steps.Add(new SagaStepResult("Compensation", true, "Nije potrebna: PENDING tokeni nisu kreirani."));
            return Results.Json(CreateSagaResponse("CheckoutSaga", "FAILED", steps, validateJson), statusCode: (int)validateResponse.StatusCode);
        }
    }

    try
    {
        using (var prepareRequest = CreateInternalTourServiceRequest(
                   HttpMethod.Post,
                   $"{tourServiceBaseUrl}/api/tours/cart/checkout/prepare",
                   userId,
                   tourServiceInternalApiKey))
        using (var prepareResponse = await httpClient.SendAsync(prepareRequest, context.RequestAborted))
        {
            var prepareJson = await prepareResponse.Content.ReadAsStringAsync(context.RequestAborted);
            checkoutId = prepareResponse.IsSuccessStatusCode ? ExtractCheckoutId(prepareJson) : null;
            var prepareSucceeded = prepareResponse.IsSuccessStatusCode && !string.IsNullOrWhiteSpace(checkoutId);
            steps.Add(new SagaStepResult("Create PENDING purchase tokens in TourService", prepareSucceeded, prepareSucceeded
                ? "PENDING tokeni su kreirani."
                : (prepareResponse.IsSuccessStatusCode ? "Prepare checkout nije vratio checkoutId." : prepareJson)));

            if (!prepareSucceeded)
            {
                steps.Add(new SagaStepResult("Compensation", true, "Nije potrebna: PENDING tokeni nisu kreirani."));
                var failurePayload = prepareResponse.IsSuccessStatusCode
                    ? SerializeMessage("Prepare checkout nije vratio checkoutId.")
                    : prepareJson;
                var failureStatusCode = prepareResponse.IsSuccessStatusCode
                    ? StatusCodes.Status500InternalServerError
                    : (int)prepareResponse.StatusCode;
                return Results.Json(CreateSagaResponse("CheckoutSaga", "FAILED", steps, failurePayload), statusCode: failureStatusCode);
            }
        }

        var paymentAuthorization = SimulatePaymentAuthorization(context.Request);
        steps.Add(new SagaStepResult("Simulate payment authorization", paymentAuthorization.Success, paymentAuthorization.Message));
        if (!paymentAuthorization.Success)
        {
            var compensation = await CompensateCheckout(
                userId,
                checkoutId!,
                httpClient,
                tourServiceBaseUrl,
                tourServiceInternalApiKey,
                context.RequestAborted);
            steps.Add(new SagaStepResult("Compensation", compensation.Success, compensation.Message));
            return Results.Json(
                CreateSagaResponse("CheckoutSaga", "FAILED", steps, SerializeMessage(paymentAuthorization.Message)),
                statusCode: paymentAuthorization.StatusCode);
        }

        using var confirmRequest = CreateInternalTourServiceRequest(
            HttpMethod.Post,
            $"{tourServiceBaseUrl}/api/tours/cart/checkout/{checkoutId}/confirm",
            userId,
            tourServiceInternalApiKey);
        using var confirmResponse = await httpClient.SendAsync(confirmRequest, context.RequestAborted);
        var confirmJson = await confirmResponse.Content.ReadAsStringAsync(context.RequestAborted);

        steps.Add(new SagaStepResult("Confirm purchase tokens and clear cart in TourService", confirmResponse.IsSuccessStatusCode, confirmResponse.IsSuccessStatusCode
            ? "Tokeni su potvrdeni, a korpa ispraznjena."
            : confirmJson));

        if (!confirmResponse.IsSuccessStatusCode)
        {
            var compensation = await CompensateCheckout(
                userId,
                checkoutId!,
                httpClient,
                tourServiceBaseUrl,
                tourServiceInternalApiKey,
                context.RequestAborted);
            steps.Add(new SagaStepResult("Compensation", compensation.Success, compensation.Message));
            return Results.Json(CreateSagaResponse("CheckoutSaga", "FAILED", steps, confirmJson), statusCode: (int)confirmResponse.StatusCode);
        }

        return Results.Json(CreateSagaResponse("CheckoutSaga", "COMPLETED", steps, confirmJson));
    }
    catch (Exception ex)
    {
        if (!string.IsNullOrWhiteSpace(checkoutId))
        {
            var compensation = await CompensateCheckout(
                userId,
                checkoutId!,
                httpClient,
                tourServiceBaseUrl,
                tourServiceInternalApiKey,
                context.RequestAborted);
            steps.Add(new SagaStepResult("Compensation", compensation.Success, compensation.Message));
        }

        steps.Add(new SagaStepResult("Unhandled failure", false, ex.Message));
        return Results.Json(
            CreateSagaResponse("CheckoutSaga", "FAILED", steps, SerializeMessage("Checkout saga je prekinuta pre potvrde kupovine.")),
            statusCode: StatusCodes.Status500InternalServerError);
    }
});
app.MapPost("/api/sagas/tours/{tourId}/execution/start", async (
    string tourId,
    HttpContext context,
    IHttpClientFactory httpClientFactory) =>
{
    var userId = GetUserId(context);
    var httpClient = httpClientFactory.CreateClient("gateway-proxy");
    var steps = new List<SagaStepResult>();
    PreparedExecutionStartResult? preparedExecution = null;

    var userValidation = await ValidateTourist(userId, httpClient, stakeholdersServiceBaseUrl, context.RequestAborted);
    AddSagaStep(steps, app.Logger, "StartTourExecutionSaga", "Validate tourist in StakeholdersService", userValidation.Success, userValidation.Message);
    if (!userValidation.Success)
    {
        return Results.Json(CreateSagaResponse("StartTourExecutionSaga", "FAILED", steps, null), statusCode: userValidation.StatusCode);
    }

    try
    {
        using (var prepareRequest = CreateInternalTourServiceRequest(
                   HttpMethod.Post,
                   $"{tourServiceBaseUrl}/api/tours/{tourId}/execution/start/prepare",
                   userId,
                   tourServiceInternalApiKey))
        using (var prepareResponse = await httpClient.SendAsync(prepareRequest, context.RequestAborted))
        {
            var prepareJson = await prepareResponse.Content.ReadAsStringAsync(context.RequestAborted);
            preparedExecution = prepareResponse.IsSuccessStatusCode
                ? ParsePreparedExecutionStart(prepareJson)
                : null;

            var prepareSucceeded = prepareResponse.IsSuccessStatusCode
                && preparedExecution is { Parsed: true };

            AddSagaStep(
                steps,
                app.Logger,
                "StartTourExecutionSaga",
                "Prepare ACTIVE execution in TourService",
                prepareSucceeded,
                prepareSucceeded
                    ? preparedExecution!.Created
                        ? "ACTIVE execution je kreiran."
                        : "Postojeci ACTIVE execution je vracen."
                    : (prepareResponse.IsSuccessStatusCode
                        ? "Prepare start execution nije vratio validan payload."
                        : prepareJson));

            if (!prepareSucceeded)
            {
                AddSagaStep(steps, app.Logger, "StartTourExecutionSaga", "Compensation", true, "Nije potrebna: execution nije pripremljen.");
                var failurePayload = prepareResponse.IsSuccessStatusCode
                    ? SerializeMessage("Prepare start execution nije vratio validan payload.")
                    : prepareJson;
                var failureStatusCode = prepareResponse.IsSuccessStatusCode
                    ? StatusCodes.Status500InternalServerError
                    : (int)prepareResponse.StatusCode;
                return Results.Json(CreateSagaResponse("StartTourExecutionSaga", "FAILED", steps, failurePayload), statusCode: failureStatusCode);
            }
        }

        using var activityRequest = CreateInternalStakeholdersRequest(
            HttpMethod.Post,
            $"{stakeholdersServiceBaseUrl}/api/users/{userId}/activities/tour-execution-started",
            stakeholdersServiceInternalApiKey);
        activityRequest.Content = JsonContent.Create(new
        {
            tourId,
            executionId = preparedExecution!.ExecutionId,
            description = $"Tour execution started for tour {tourId}."
        });

        if (ShouldSimulateExecutionActivityFailure(context.Request))
        {
            activityRequest.Headers.TryAddWithoutValidation(SimulateExecutionActivityFailureHeader, "true");
        }

        using var activityResponse = await httpClient.SendAsync(activityRequest, context.RequestAborted);
        var activityJson = await activityResponse.Content.ReadAsStringAsync(context.RequestAborted);

        AddSagaStep(
            steps,
            app.Logger,
            "StartTourExecutionSaga",
            "Register tour execution activity in StakeholdersService",
            activityResponse.IsSuccessStatusCode,
            activityResponse.IsSuccessStatusCode
                ? "Aktivnost pokretanja ture je upisana."
                : activityJson);

        if (!activityResponse.IsSuccessStatusCode)
        {
            var compensationMessage = preparedExecution.Created
                ? await CompensateStartExecution(
                    userId,
                    tourId,
                    httpClient,
                    tourServiceBaseUrl,
                    tourServiceInternalApiKey,
                    context.RequestAborted)
                : new CompensationResult(true, "Nije potrebna: ACTIVE execution je postojao pre sage.");
            AddSagaStep(steps, app.Logger, "StartTourExecutionSaga", "Compensation", compensationMessage.Success, compensationMessage.Message);
            return Results.Json(CreateSagaResponse("StartTourExecutionSaga", "FAILED", steps, activityJson), statusCode: (int)activityResponse.StatusCode);
        }

        return Results.Json(CreateSagaResponse("StartTourExecutionSaga", "COMPLETED", steps, preparedExecution.ExecutionJson));
    }
    catch (Exception ex)
    {
        app.Logger.LogError(ex, "StartTourExecutionSaga interrupted. userId={UserId}, tourId={TourId}", userId, tourId);
        if (preparedExecution is { Created: true })
        {
            var compensation = await CompensateStartExecution(
                userId,
                tourId,
                httpClient,
                tourServiceBaseUrl,
                tourServiceInternalApiKey,
                context.RequestAborted);
            AddSagaStep(steps, app.Logger, "StartTourExecutionSaga", "Compensation", compensation.Success, compensation.Message);
        }
        else
        {
            AddSagaStep(steps, app.Logger, "StartTourExecutionSaga", "Compensation", true, "Nije potrebna: ACTIVE execution nije kreiran u ovoj sagi.");
        }

        return Results.Json(
            CreateSagaResponse("StartTourExecutionSaga", "FAILED", steps, SerializeMessage("Start tour execution saga je prekinuta pre finalizacije.")),
            statusCode: StatusCodes.Status500InternalServerError);
    }
});

app.Map("/{**path}", async (
    HttpContext context,
    IHttpClientFactory httpClientFactory) =>
{
    var sagaRoute = GetSagaRouteForBlockedPublicTourMutation(context.Request);
    if (sagaRoute is not null)
    {
        context.Response.StatusCode = StatusCodes.Status404NotFound;
        await context.Response.WriteAsJsonAsync(new
        {
            message = $"Ruta nije javno dostupna. Koristite {sagaRoute}."
        });
        return;
    }

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

static string? GetSagaRouteForBlockedPublicTourMutation(HttpRequest request)
{
    if (!HttpMethods.IsPost(request.Method))
    {
        return null;
    }

    var path = NormalizePath(request.Path.Value);
    if (path.Equals("/api/tours/cart/checkout", StringComparison.OrdinalIgnoreCase))
    {
        return "/api/sagas/checkout";
    }

    var match = Regex.Match(path, "^/api/tours/([0-9a-fA-F]{24})/execution/start$", RegexOptions.IgnoreCase | RegexOptions.CultureInvariant);
    return match.Success ? $"/api/sagas/tours/{match.Groups[1].Value}/execution/start" : null;
}

static string NormalizePath(string? path)
{
    if (string.IsNullOrWhiteSpace(path))
    {
        return "/";
    }

    var normalized = path.TrimEnd('/');
    return string.IsNullOrWhiteSpace(normalized) ? "/" : normalized;
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

static IResult GrpcFailure(RpcException ex)
{
    return Results.Json(new { message = ex.Status.Detail }, statusCode: MapGrpcStatusCode(ex.StatusCode));
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

static object MapShoppingCartReply(ShoppingCartReply cart)
{
    return new
    {
        touristId = cart.TouristId,
        items = cart.Items.Select(MapOrderItemReply).ToArray(),
        totalPrice = cart.TotalPrice
    };
}

static object MapOrderItemReply(OrderItemReply item)
{
    return new
    {
        tourId = item.TourId,
        tourName = item.TourName,
        price = item.Price
    };
}

static object MapPurchaseTokenListReply(PurchaseTokenListReply reply)
{
    return reply.Tokens.Select(MapPurchaseTokenReply).ToArray();
}

static object MapPurchaseTokenReply(TourPurchaseTokenReply token)
{
    return new
    {
        tourId = token.TourId,
        token = token.Token,
        status = token.Status,
        checkoutId = EmptyToNull(token.CheckoutId),
        createdAt = EmptyToNull(token.CreatedAt)
    };
}

static object MapExecutionReply(TourExecutionReply execution)
{
    return new
    {
        id = execution.Id,
        tourId = execution.TourId,
        touristId = execution.TouristId,
        status = execution.Status,
        startLatitude = execution.StartLatitude,
        startLongitude = execution.StartLongitude,
        startedAt = EmptyToNull(execution.StartedAt),
        completedAt = EmptyToNull(execution.CompletedAt),
        abandonedAt = EmptyToNull(execution.AbandonedAt),
        lastActivity = EmptyToNull(execution.LastActivity),
        completedKeyPoints = execution.CompletedKeyPoints.Select(MapCompletedKeyPointReply).ToArray()
    };
}

static object MapCompletedKeyPointReply(CompletedKeyPointReply keyPoint)
{
    return new
    {
        keyPointId = keyPoint.KeyPointId,
        keyPointName = keyPoint.KeyPointName,
        reachedAt = EmptyToNull(keyPoint.ReachedAt)
    };
}

static string? EmptyToNull(string value)
{
    return string.IsNullOrWhiteSpace(value) ? null : value;
}

static CreateTourRequest BuildCreateTourRequest(int userId, CreateTourGatewayRequest request)
{
    var grpcRequest = new CreateTourRequest
    {
        UserId = userId,
        Name = request.Name ?? string.Empty,
        Description = request.Description ?? string.Empty,
        Price = request.Price,
        Difficulty = request.Difficulty ?? string.Empty
    };

    if (request.Tags is not null)
    {
        foreach (var tag in request.Tags)
        {
            grpcRequest.Tags.Add(tag);
        }
    }

    return grpcRequest;
}

static UpdateTourRequest BuildUpdateTourRequest(string id, int userId, UpdateTourGatewayRequest request)
{
    var grpcRequest = new UpdateTourRequest
    {
        Id = id,
        UserId = userId
    };

    if (request.Name is not null)
    {
        grpcRequest.Name = request.Name;
    }
    if (request.Description is not null)
    {
        grpcRequest.Description = request.Description;
    }
    if (request.Price.HasValue)
    {
        grpcRequest.Price = request.Price.Value;
    }
    if (request.Difficulty is not null)
    {
        grpcRequest.Difficulty = request.Difficulty;
    }
    if (request.Status is not null)
    {
        grpcRequest.Status = request.Status;
    }
    if (request.TransportDurations is not null)
    {
        grpcRequest.TransportDurations = new TransportDurationsInput();
        foreach (var duration in request.TransportDurations)
        {
            grpcRequest.TransportDurations.Values.Add(duration.Key, duration.Value);
        }
    }
    if (request.Tags is not null)
    {
        grpcRequest.Tags = new StringList();
        foreach (var tag in request.Tags)
        {
            grpcRequest.Tags.Values.Add(tag);
        }
    }

    return grpcRequest;
}

static AddKeyPointRequest BuildAddKeyPointRequest(string tourId, int userId, CreateKeyPointGatewayRequest request)
{
    var grpcRequest = new AddKeyPointRequest
    {
        TourId = tourId,
        UserId = userId,
        Name = request.Name ?? string.Empty,
        Description = request.Description ?? string.Empty,
        ImageUrl = request.ImageUrl ?? string.Empty,
        Order = request.Order
    };

    if (request.Latitude.HasValue)
    {
        grpcRequest.Latitude = request.Latitude.Value;
    }
    if (request.Longitude.HasValue)
    {
        grpcRequest.Longitude = request.Longitude.Value;
    }

    return grpcRequest;
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

static HttpRequestMessage CreateInternalTourServiceRequest(HttpMethod method, string uri, int userId, string internalApiKey)
{
    var request = new HttpRequestMessage(method, new Uri(uri));
    request.Headers.TryAddWithoutValidation("X-User-Id", userId.ToString());
    request.Headers.TryAddWithoutValidation(TourServiceInternalApiKeyHeader, internalApiKey);
    return request;
}

static HttpRequestMessage CreateInternalStakeholdersRequest(HttpMethod method, string uri, string internalApiKey)
{
    var request = new HttpRequestMessage(method, new Uri(uri));
    request.Headers.TryAddWithoutValidation(TourServiceInternalApiKeyHeader, internalApiKey);
    return request;
}

static string? ExtractCheckoutId(string payload)
{
    if (string.IsNullOrWhiteSpace(payload))
    {
        return null;
    }

    try
    {
        using var document = JsonDocument.Parse(payload);
        return document.RootElement.TryGetProperty("checkoutId", out var checkoutId)
            ? checkoutId.GetString()
            : null;
    }
    catch
    {
        return null;
    }
}

static string SerializeMessage(string message)
{
    return JsonSerializer.Serialize(new { message });
}

static PreparedExecutionStartResult? ParsePreparedExecutionStart(string payload)
{
    if (string.IsNullOrWhiteSpace(payload))
    {
        return null;
    }

    try
    {
        using var document = JsonDocument.Parse(payload);
        var root = document.RootElement;
        if (!root.TryGetProperty("execution", out var execution) ||
            !root.TryGetProperty("created", out var created) ||
            !execution.TryGetProperty("id", out var executionId))
        {
            return new PreparedExecutionStartResult(false, false, null, null);
        }

        var executionIdValue = executionId.GetString();
        if (string.IsNullOrWhiteSpace(executionIdValue))
        {
            return new PreparedExecutionStartResult(false, false, null, null);
        }

        return new PreparedExecutionStartResult(
            true,
            created.GetBoolean(),
            execution.GetRawText(),
            executionIdValue);
    }
    catch
    {
        return new PreparedExecutionStartResult(false, false, null, null);
    }
}

static PaymentAuthorizationResult SimulatePaymentAuthorization(HttpRequest request)
{
    var failureHeader = request.Headers[SimulatePaymentFailureHeader].FirstOrDefault();
    var failureQuery = request.Query["simulatePaymentFailure"].FirstOrDefault();
    var shouldFail = string.Equals(failureHeader, "true", StringComparison.OrdinalIgnoreCase)
        || string.Equals(failureQuery, "true", StringComparison.OrdinalIgnoreCase);

    return shouldFail
        ? new PaymentAuthorizationResult(false, StatusCodes.Status502BadGateway, "Simulirana autorizacija placanja nije uspela.")
        : new PaymentAuthorizationResult(true, StatusCodes.Status200OK, "Simulirana autorizacija placanja je uspesna.");
}

static bool ShouldSimulateExecutionActivityFailure(HttpRequest request)
{
    var failureHeader = request.Headers[SimulateExecutionActivityFailureHeader].FirstOrDefault();
    var failureQuery = request.Query["simulateExecutionActivityFailure"].FirstOrDefault();
    return string.Equals(failureHeader, "true", StringComparison.OrdinalIgnoreCase)
        || string.Equals(failureQuery, "true", StringComparison.OrdinalIgnoreCase);
}

static async Task<CompensationResult> CompensateCheckout(
    int userId,
    string checkoutId,
    HttpClient httpClient,
    string tourServiceBaseUrl,
    string internalApiKey,
    CancellationToken cancellationToken)
{
    using var compensationRequest = CreateInternalTourServiceRequest(
        HttpMethod.Post,
        $"{tourServiceBaseUrl}/api/tours/cart/checkout/{checkoutId}/cancel",
        userId,
        internalApiKey);
    using var compensationResponse = await httpClient.SendAsync(compensationRequest, cancellationToken);
    var compensationJson = await compensationResponse.Content.ReadAsStringAsync(cancellationToken);
    return new CompensationResult(
        compensationResponse.IsSuccessStatusCode,
        compensationResponse.IsSuccessStatusCode
            ? "PENDING tokeni su oznaceni kao CANCELLED, a korpa je sacuvana."
            : compensationJson);
}

static async Task<CompensationResult> CompensateStartExecution(
    int userId,
    string tourId,
    HttpClient httpClient,
    string tourServiceBaseUrl,
    string internalApiKey,
    CancellationToken cancellationToken)
{
    using var compensationRequest = CreateInternalTourServiceRequest(
        HttpMethod.Post,
        $"{tourServiceBaseUrl}/api/tours/{tourId}/execution/start/cancel",
        userId,
        internalApiKey);
    using var compensationResponse = await httpClient.SendAsync(compensationRequest, cancellationToken);
    var compensationJson = await compensationResponse.Content.ReadAsStringAsync(cancellationToken);
    return new CompensationResult(
        compensationResponse.IsSuccessStatusCode,
        compensationResponse.IsSuccessStatusCode
            ? "ACTIVE execution je kompenzovan i prebacen u ABANDONED."
            : compensationJson);
}

static void AddSagaStep(List<SagaStepResult> steps, ILogger logger, string saga, string step, bool success, string message)
{
    steps.Add(new SagaStepResult(step, success, message));
    if (success)
    {
        logger.LogInformation("{Saga} step succeeded: {Step}. {Message}", saga, step, message);
    }
    else
    {
        logger.LogWarning("{Saga} step failed: {Step}. {Message}", saga, step, message);
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

sealed record PaymentAuthorizationResult(bool Success, int StatusCode, string Message);

sealed record CompensationResult(bool Success, string Message);

sealed record PreparedExecutionStartResult(bool Parsed, bool Created, string? ExecutionJson, string? ExecutionId);

sealed record CreateTourGatewayRequest(string? Name, string? Description, double Price, string? Difficulty, List<string>? Tags);

sealed record UpdateTourGatewayRequest(
    string? Name,
    string? Description,
    double? Price,
    string? Difficulty,
    string? Status,
    Dictionary<string, int>? TransportDurations,
    List<string>? Tags);

sealed record CreateKeyPointGatewayRequest(
    string? Name,
    string? Description,
    double? Latitude,
    double? Longitude,
    string? ImageUrl,
    int Order);

