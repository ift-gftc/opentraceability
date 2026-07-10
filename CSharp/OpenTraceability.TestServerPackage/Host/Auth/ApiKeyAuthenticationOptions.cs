using Microsoft.AspNetCore.Authentication;

namespace OpenTraceability.TestServer.Auth
{
    public class ApiKeyAuthenticationOptions : AuthenticationSchemeOptions
    {
        public const string SchemeName = "ApiKey";
        public string HeaderName { get; set; } = "X-API-Key";
    }
}
