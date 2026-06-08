using System.Threading.Tasks;

namespace OpenTraceability.TestServer.Auth
{
    public interface IApiKeyStore
    {
        /// <summary>
        /// True when at least one API key is configured. When false, API key authentication is not
        /// enforced and all requests are treated as authenticated.
        /// </summary>
        bool HasConfiguredKeys { get; }

        Task<bool> IsValidKeyAsync(string key);
    }
}
