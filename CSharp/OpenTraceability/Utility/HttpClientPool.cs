namespace OpenTraceability.Utility
{
    /// <summary>
    /// This is a helper class to grab an HTTP client from a pool so that we don't have to create a new one every time we need to make a request.
    /// </summary>
    public static class HttpClientPool
    {
        private static LimitedPool<HttpClient> _clientPool = new LimitedPool<HttpClient>(() =>
        {
#if DEBUG
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            httpClientHandler.ServerCertificateCustomValidationCallback += (m, e, c, h) =>
            {
                return true;
            };
            var hc = new HttpClient(httpClientHandler);
#else
            var hc = new HttpClient();
#endif
            return hc;
        },
        (c) => c.Dispose(), TimeSpan.FromMinutes(5));

        public static LimitedPoolItem<HttpClient> GetClient()
        {
            return _clientPool.Get();
        }
    }
}