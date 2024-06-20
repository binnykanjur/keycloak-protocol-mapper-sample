using System.Text.Json.Serialization;

var builder = WebApplication.CreateBuilder(args);

var app = builder.Build();

app.MapGet("/api/v1/claims", (string objectId) => {
    return Results.Ok(
        new ClaimsResponse {
            UserId = objectId,
            Role = $"Role_{Random.Shared.Next(1, 100)}"
        });
});

app.Run();

internal class ClaimsResponse {
    [JsonPropertyName("__user__")]
    public required string UserId { get; set; }
    public required string Role { get; set; }
}