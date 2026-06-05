using System.Collections.Generic;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Http.Extensions;
using Microsoft.Extensions.Configuration;
using OpenTraceability.TestServer.Core.Modules;

namespace OpenTraceability.TestServer.Infrastructure
{
    /// <summary>
    /// The set of modules this server instance is configured to support (always includes Core,
    /// and expands Wildcaught/Aquaculture to also include Seafood). Registered as a singleton.
    /// </summary>
    public class SupportedModules
    {
        public HashSet<GdstModule> Modules { get; }

        public SupportedModules(IConfiguration config)
        {
            var names = config.GetSection("Modules").Get<List<string>>() ?? new List<string>();
            Modules = ModuleSet.Parse(names);
        }
    }

    /// <summary>
    /// Helpers for resolving the dataset id and base URL from a request.
    /// </summary>
    public static class RequestContextExtensions
    {
        public const string DatasetHeader = "X-Dataset-Id";

        /// <summary>
        /// The dataset (blob) id from the optional X-Dataset-Id header, or "default".
        /// </summary>
        public static string GetDatasetId(this HttpRequest request)
        {
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
