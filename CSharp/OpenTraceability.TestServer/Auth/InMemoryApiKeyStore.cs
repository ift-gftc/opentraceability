using System.Collections.Generic;
using System.Threading.Tasks;
using Microsoft.Extensions.Configuration;

namespace OpenTraceability.TestServer.Auth
{
    /// <summary>
    /// Validates API keys against the list configured under Authentication:APIKey:ValidKeys.
    /// </summary>
    public class InMemoryApiKeyStore : IApiKeyStore
    {
        private readonly List<string> _validKeys;

        public InMemoryApiKeyStore(IConfiguration config)
        {
            _validKeys = config.GetSection("Authentication:APIKey:ValidKeys").Get<List<string>>() ?? new List<string>();
        }

        public Task<bool> IsValidKeyAsync(string key) => Task.FromResult(_validKeys.Contains(key));
    }
}
