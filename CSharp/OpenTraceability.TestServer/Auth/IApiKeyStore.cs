using System.Threading.Tasks;

namespace OpenTraceability.TestServer.Auth
{
    public interface IApiKeyStore
    {
        Task<bool> IsValidKeyAsync(string key);
    }
}
