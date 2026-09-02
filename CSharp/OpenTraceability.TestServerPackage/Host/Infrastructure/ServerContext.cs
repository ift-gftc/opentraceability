namespace OpenTraceability.TestServer.Infrastructure
{
    /// <summary>
    /// Helpers for resolving the dataset id and base URL from a request.
    /// </summary>
    public static class RequestContextExtensions
    {
        public const string DatasetHeader = "X-Dataset-Id";
        public const string DatasetRouteKey = "datasetId";

        /// <summary>
        /// The dataset (blob) id from the optional {datasetId} route segment, falling back to the
        /// optional X-Dataset-Id header, or "default".
        /// </summary>
        public static string GetDatasetId(this HttpRequest request)
        {
            if (request.RouteValues.TryGetValue(DatasetRouteKey, out var routeValue))
            {
                var routeId = routeValue?.ToString();
                if (!string.IsNullOrWhiteSpace(routeId)) return routeId;
            }
            if (request.Headers.TryGetValue(DatasetHeader, out var value))
            {
                var id = value.ToString();
                if (!string.IsNullOrWhiteSpace(id)) return id;
            }
            return "default";
        }

        /// <summary>
        /// The base URL used when building digital links: the configured BaseURL, otherwise derived
        /// from the current request.
        /// </summary>
        public static string GetBaseUrl(this HttpRequest request, IConfiguration config)
        {
            var configured = config["BaseURL"];
            if (!string.IsNullOrWhiteSpace(configured)) return configured.TrimEnd('/');
            return $"{request.Scheme}://{request.Host.Value}";
        }
    }
}
