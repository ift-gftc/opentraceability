using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Utility
{
    /// <summary>
    /// This is a helper class to grab an HTTP client from a pool so that we don't have to create a new one every time we need to make a request.
    /// </summary>
    public static class HttpClientPool
    {
        static LimitedPool<HttpClient> _clientPool = new LimitedPool<HttpClient>(() => new HttpClient(), (c) => c.Dispose(), TimeSpan.FromMinutes(5));

        public static LimitedPoolItem<HttpClient> GetClient()
        {
            return _clientPool.Get();
        }
    }
}
