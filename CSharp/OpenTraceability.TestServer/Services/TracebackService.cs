using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading.Tasks;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Queries;
using OpenTraceability.TestServer.Core.Data;

namespace OpenTraceability.TestServer.Services
{
    /// <summary>The configuration of an external EPCIS/digital-link server to trace back against.</summary>
    public class TracebackRequest
    {
        /// <summary>The EPCs to start the traceback from.</summary>
        public List<string> Epcs { get; set; } = new List<string>();

        /// <summary>The external server's digital link resolver URL.</summary>
        public string ResolverUrl { get; set; } = string.Empty;

        /// <summary>Optional API key for the external server.</summary>
        public string? ApiKey { get; set; }

        /// <summary>"JSON" (default) or "XML".</summary>
        public string Format { get; set; } = "JSON";

        /// <summary>"2.0" (default) or "1.2".</summary>
        public string Version { get; set; } = "2.0";

        /// <summary>Optional dataset to store the retrieved data under. Defaults to "default".</summary>
        public string? DatasetId { get; set; }

        public string? CapabilityProcessUUID { get; set; } = string.Empty;
    }

    public class TracebackResult
    {
        public int EventsStored { get; set; }
        public int MasterDataStored { get; set; }
        public List<string> Errors { get; set; } = new List<string>();
    }

    /// <summary>
    /// Executes EPCIS tracebacks against an external server and stores the retrieved data locally.
    /// </summary>
    public class TracebackService
    {
        private readonly ITraceabilityStore _store;
        private readonly IHttpClientFactory _httpClientFactory;

        public TracebackService(ITraceabilityStore store, IHttpClientFactory httpClientFactory)
        {
            _store = store;
            _httpClientFactory = httpClientFactory;
        }

        public async Task<TracebackResult> ExecuteAsync(TracebackRequest request)
        {
            var result = new TracebackResult();
            string datasetId = string.IsNullOrWhiteSpace(request.DatasetId) ? "default" : request.DatasetId!;

            var format = string.Equals(request.Format, "XML", StringComparison.OrdinalIgnoreCase)
                ? EPCISDataFormat.XML : EPCISDataFormat.JSON;
            var version = request.Version == "1.2" ? EPCISVersion.V1 : EPCISVersion.V2;

            var client = _httpClientFactory.CreateClient();
            if (!string.IsNullOrWhiteSpace(request.ApiKey))
            {
                client.DefaultRequestHeaders.Add("X-API-Key", request.ApiKey);
            }
            if (!string.IsNullOrEmpty(request.CapabilityProcessUUID))
            {
                client.DefaultRequestHeaders.Add("X-Capability-Process-UUID", request.CapabilityProcessUUID);
            }

            var dlOptions = new DigitalLinkQueryOptions
            {
                URL = new Uri(request.ResolverUrl),
                APIKey = request.ApiKey,
                Format = format,
                Version = version
            };

            var merged = new EPCISDocument { EPCISVersion = version };

            foreach (var epcStr in request.Epcs)
            {
                try
                {
                    var epc = new EPC(epcStr);
                    var epcisUrl = await EPCISTraceabilityResolver.GetEPCISQueryInterfaceURL(dlOptions, epc, client);
                    if (epcisUrl == null)
                    {
                        result.Errors.Add($"Could not resolve EPCIS query interface for {epcStr}");
                        continue;
                    }

                    var opts = new EPCISQueryInterfaceOptions
                    {
                        URL = epcisUrl,
                        APIKey = request.ApiKey,
                        Format = format,
                        Version = version
                    };

                    var results = await EPCISTraceabilityResolver.Traceback(opts, epc, client);
                    if (results.Document != null)
                    {
                        merged.Merge(results.Document);
                    }
                    foreach (var err in results.Errors)
                    {
                        result.Errors.Add(err.Details ?? err.Type.ToString());
                    }
                }
                catch (Exception ex)
                {
                    result.Errors.Add($"{epcStr}: {ex.Message}");
                }
            }

            // Resolve any referenced master data and store everything.
            try
            {
                await MasterDataResolver.ResolveMasterData(dlOptions, merged, client);
            }
            catch (Exception ex)
            {
                result.Errors.Add($"master data resolution: {ex.Message}");
            }

            if (merged.Events.Count > 0)
            {
                await _store.UpsertEventsAsync(datasetId, merged.Events, merged.Namespaces);
            }
            if (merged.MasterData.Count > 0)
            {
                await _store.UpsertMasterDataAsync(datasetId, merged.MasterData);
            }

            result.EventsStored = merged.Events.Count;
            result.MasterDataStored = merged.MasterData.Count;
            return result;
        }
    }
}
