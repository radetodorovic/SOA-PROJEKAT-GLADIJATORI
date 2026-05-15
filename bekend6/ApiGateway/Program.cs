using System.Net.Http.Headers;

var builder = WebApplication.CreateBuilder(args);

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

var routes = new[]
{
    new GatewayRoute("/api/auth", GetServiceBaseUrl(builder.Configuration, "StakeholdersService", "http://localhost:5008")),
    new GatewayRoute("/api/users", GetServiceBaseUrl(builder.Configuration, "StakeholdersService", "http://localhost:5008")),
    new GatewayRoute("/api/blogs", GetServiceBaseUrl(builder.Configuration, "BlogService", "http://localhost:5009")),
    new GatewayRoute("/api/followers", GetServiceBaseUrl(builder.Configuration, "FollowerService", "http://localhost:5010")),
    new GatewayRoute("/api/tours", GetServiceBaseUrl(builder.Configuration, "TourService", "http://localhost:5011"))
};

app.MapGet("/health", () => Results.Ok(new
{
    status = "ok",
    service = "api-gateway"
}));

app.MapGet("/", () => Results.Ok(CreateGatewayInfo(routes)));
app.MapGet("/api", () => Results.Ok(CreateGatewayInfo(routes)));

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
    var key = $"Services:{serviceName}:BaseUrl";
    var configured = configuration[key];
    return string.IsNullOrWhiteSpace(configured) ? fallback : configured;
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
