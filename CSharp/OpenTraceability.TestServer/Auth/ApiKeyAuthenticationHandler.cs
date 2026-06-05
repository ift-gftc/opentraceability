using System.Security.Claims;
using System.Text.Encodings.Web;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Authentication;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace OpenTraceability.TestServer.Auth
{
    /// <summary>
    /// API key authentication: reads the key from the configured header (default X-API-Key) or a
    /// query parameter and validates it against the <see cref="IApiKeyStore"/>.
    /// </summary>
    public class ApiKeyAuthenticationHandler : AuthenticationHandler<ApiKeyAuthenticationOptions>
    {
        private readonly IApiKeyStore _apiKeyStore;

        public ApiKeyAuthenticationHandler(
            IOptionsMonitor<ApiKeyAuthenticationOptions> options,
            ILoggerFactory logger,
            UrlEncoder encoder,
            IApiKeyStore apiKeyStore)
            : base(options, logger, encoder)
        {
            _apiKeyStore = apiKeyStore;
        }

        protected override async Task<AuthenticateResult> HandleAuthenticateAsync()
        {
            string apiKey = string.Empty;

            if (Request.Headers.TryGetValue(Options.HeaderName, out var headerValue))
            {
                apiKey = headerValue.FirstOrDefault()?.ToString() ?? string.Empty;
            }

            if (string.IsNullOrWhiteSpace(apiKey))
            {
                apiKey = Request.Query[Options.HeaderName].FirstOrDefault()?.ToString() ?? string.Empty;
            }

            if (string.IsNullOrWhiteSpace(apiKey))
            {
                return AuthenticateResult.Fail("Missing API key");
            }

            if (!await _apiKeyStore.IsValidKeyAsync(apiKey))
            {
                return AuthenticateResult.Fail("Invalid API key");
            }

            var claims = new[] { new Claim(ClaimTypes.Name, "ApiKeyUser") };
            var identity = new ClaimsIdentity(claims, Scheme.Name);
            var principal = new ClaimsPrincipal(identity);
            var ticket = new AuthenticationTicket(principal, Scheme.Name);
            return AuthenticateResult.Success(ticket);
        }
    }
}
