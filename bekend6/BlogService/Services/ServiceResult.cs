namespace BlogService.Services;

public class ServiceResult<T>
{
    public bool IsSuccess { get; private init; }
    public int StatusCode { get; private init; }
    public string Message { get; private init; } = string.Empty;
    public T? Data { get; private init; }

    public static ServiceResult<T> Success(T data, int statusCode = StatusCodes.Status200OK, string message = "") =>
        new()
        {
            IsSuccess = true,
            StatusCode = statusCode,
            Message = message,
            Data = data
        };

    public static ServiceResult<T> Failure(string message, int statusCode) =>
        new()
        {
            IsSuccess = false,
            StatusCode = statusCode,
            Message = message
        };
}
