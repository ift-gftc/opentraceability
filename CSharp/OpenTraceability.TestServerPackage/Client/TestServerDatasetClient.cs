using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Queries;
using OpenTraceability.TestServer.Core.Models;

namespace OpenTraceability.TestServer.Core.Client
{
    /// <summary>
    /// Data operations against one dataset of the test server. Every call uses the
    /// /{datasetId}/... path prefix — the same code path a GDST capability tool exercises when it
    /// is handed <see cref="DigitalLinkUrl"/> as the resolver root — so behavior matches a real
    /// capability run exactly. Obtain via <see cref="TestServerClient.ForDataset"/>.
    /// </summary>
    public sealed class TestServerDatasetClient
    {
        private readonly TestServerClient _client;

        public string DatasetId { get; }

        /// <summary>
        /// The dataset-scoped digital link resolver root ({baseUrl}/{datasetId}/digitallink/).
        /// Hand this to the GDST capability tool (or any resolver consumer) to pin it to this dataset.
        /// </summary>
        public string DigitalLinkUrl => $"{_client.BaseUrl}/{DatasetId}/digitallink/";

        internal TestServerDatasetClient(TestServerClient client, string datasetId)
        {
            _client = client;
            DatasetId = datasetId;
        }

        private string Prefix => "/" + DatasetId;

        // ---- EPCIS ----

        /// <summary>POST /{datasetId}/epcis/events. Returns the number of events stored.</summary>
        public async Task<int> PostEpcisDocumentAsync(EPCISDocument document, EPCISDataFormat format = EPCISDataFormat.JSON, EPCISVersion version = EPCISVersion.V2, CancellationToken ct = default)
        {
            var mapper = format == EPCISDataFormat.JSON
                ? OpenTraceabilityMappers.EPCISDocument.JSON
                : OpenTraceabilityMappers.EPCISDocument.XML;
            string raw = mapper.Map(document);
            string versionHeader = version == EPCISVersion.V1 ? "1.2" : "2.0";
            return await PostEpcisDocumentAsync(raw, format, versionHeader, ct);
        }

        /// <summary>POST /{datasetId}/epcis/events with a raw EPCIS document body.</summary>
        public async Task<int> PostEpcisDocumentAsync(string rawDocument, EPCISDataFormat format, string epcisVersionHeader = "2.0", CancellationToken ct = default)
        {
            string contentType = format == EPCISDataFormat.JSON ? "application/json" : "application/xml";
            var content = new StringContent(rawDocument, Encoding.UTF8, contentType);

            string body = await _client.SendForBodyAsync(HttpMethod.Post, Prefix + "/epcis/events", content, ct,
                req => req.Headers.Add("GS1-EPCIS-Version", epcisVersionHeader));
            return JObject.Parse(body)["eventsStored"]?.Value<int>() ?? 0;
        }

        /// <summary>GET /{datasetId}/epcis/events — returns the module-minified EPCIS Query Document.</summary>
        public async Task<EPCISQueryDocument> QueryEventsAsync(EPCISQueryParameters parameters, CancellationToken ct = default)
        {
            string json = await QueryEventsRawAsync(parameters, ct);
            // The server's responses are minified to the dataset's modules; not schema-checked here
            // because minified documents are intentionally partial.
            return OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(json, checkSchema: false);
        }

        /// <summary>GET /{datasetId}/epcis/events — returns the raw module-minified JSON.</summary>
        public Task<string> QueryEventsRawAsync(EPCISQueryParameters parameters, CancellationToken ct = default)
            => _client.SendForBodyAsync(HttpMethod.Get, Prefix + "/epcis/events" + parameters.ToQueryParameters(), null, ct);

        // ---- master data ----

        /// <summary>POST /{datasetId}/masterdata with GS1 Web Vocab JSON-LD (single object or array). Returns the number stored.</summary>
        public async Task<int> PostMasterDataAsync(string gs1WebVocabJson, CancellationToken ct = default)
        {
            var content = new StringContent(gs1WebVocabJson, Encoding.UTF8, "application/json");
            string body = await _client.SendForBodyAsync(HttpMethod.Post, Prefix + "/masterdata", content, ct);
            return JObject.Parse(body)["masterDataStored"]?.Value<int>() ?? 0;
        }

        /// <summary>
        /// GET /{datasetId}/masterdata/{type}/{identifier} — returns the module-minified GS1 Web
        /// Vocab JSON-LD, or null when not found. Type: product/tradeitem, location, party/tradingparty.
        /// </summary>
        public async Task<string?> GetMasterDataAsync(string type, string identifier, CancellationToken ct = default)
        {
            using var resp = await _client.SendAsync(HttpMethod.Get, Prefix + $"/masterdata/{type}/{Uri.EscapeDataString(identifier)}", null, ct);
            if (resp.StatusCode == HttpStatusCode.NotFound) return null;
            return await TestServerClient.EnsureSuccessAsync(resp, "GET", Prefix + "/masterdata/" + type);
        }

        /// <summary>GET /{datasetId}/masterdata/{type} — returns all definitions of a type as module-minified JSON.</summary>
        public Task<string> GetMasterDataDefinitionsAsync(string type, CancellationToken ct = default)
            => _client.SendForBodyAsync(HttpMethod.Get, Prefix + $"/masterdata/{type}", null, ct);

        // ---- digital link ----

        public Task<List<DigitalLink>> GetProductLinksAsync(string gtin, string? linkType = null, CancellationToken ct = default)
            => GetLinksAsync($"01/{Uri.EscapeDataString(gtin)}", linkType, ct);

        public Task<List<DigitalLink>> GetEpcClassLinksAsync(string gtin, string lot, string? linkType = null, CancellationToken ct = default)
            => GetLinksAsync($"01/{Uri.EscapeDataString(gtin)}/10/{Uri.EscapeDataString(lot)}", linkType, ct);

        public Task<List<DigitalLink>> GetEpcInstanceLinksAsync(string gtin, string serial, string? linkType = null, CancellationToken ct = default)
            => GetLinksAsync($"01/{Uri.EscapeDataString(gtin)}/21/{Uri.EscapeDataString(serial)}", linkType, ct);

        public Task<List<DigitalLink>> GetSsccLinksAsync(string sscc, string? linkType = null, CancellationToken ct = default)
            => GetLinksAsync($"00/{Uri.EscapeDataString(sscc)}", linkType, ct);

        public Task<List<DigitalLink>> GetLocationLinksAsync(string gln, string? linkType = null, CancellationToken ct = default)
            => GetLinksAsync($"414/{Uri.EscapeDataString(gln)}", linkType, ct);

        public Task<List<DigitalLink>> GetPartyLinksAsync(string pgln, string? linkType = null, CancellationToken ct = default)
            => GetLinksAsync($"417/{Uri.EscapeDataString(pgln)}", linkType, ct);

        private async Task<List<DigitalLink>> GetLinksAsync(string identifierPath, string? linkType, CancellationToken ct)
        {
            string path = Prefix + "/digitallink/" + identifierPath;
            if (!string.IsNullOrEmpty(linkType))
            {
                path += "?linkType=" + Uri.EscapeDataString(linkType);
            }
            string body = await _client.SendForBodyAsync(HttpMethod.Get, path, null, ct);
            return JsonConvert.DeserializeObject<List<DigitalLink>>(body) ?? new List<DigitalLink>();
        }

        // ---- traceback ----

        /// <summary>
        /// POST /{datasetId}/traceback — traces the EPCs back against an external resolver and stores
        /// the retrieved events/master data into this dataset.
        /// </summary>
        public async Task<TracebackResult> RunTracebackAsync(TracebackRequest request, CancellationToken ct = default)
        {
            request.DatasetId ??= DatasetId;
            string body = await _client.SendForBodyAsync(HttpMethod.Post, Prefix + "/traceback", TestServerClient.JsonContent(request), ct);
            return JsonConvert.DeserializeObject<TracebackResult>(body) ?? new TracebackResult();
        }
    }
}
