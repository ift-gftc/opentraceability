using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Newtonsoft.Json;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Queries;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.GDST.Modules;
using OpenTraceability.TestServer.Core.Models;
using OpenTraceability.TestServer.Core.Services;
using WireMock.Matchers;
using WireMock.RequestBuilders;
using WireMock.ResponseBuilders;
using WireMock.Server;
using WireMock.Types;
using WireMock.Util;
using WireMockResponse = global::WireMock.ResponseMessage;
using WireMockRequest = global::WireMock.IRequestMessage;
using OpenTraceability.GDST;

namespace OpenTraceability.TestServer.Core.WireMock
{
    /// <summary>
    /// An in-process WireMock-backed traceability server for unit/integration testing in external
    /// .NET projects. Resolves digital links, runs EPCIS queries, and serves master data against an
    /// in-memory SQLite database using the same core services as the real Docker server. Like the
    /// real server, datasets are persisted records carrying their own module set: requests under
    /// /{datasetId}/... are served (and minified) with that dataset's modules, and unknown datasets
    /// return 404.
    /// </summary>
    public sealed class WireMockTraceabilityServer : IDisposable
    {
        private static readonly HashSet<string> _knownRoots = new(StringComparer.OrdinalIgnoreCase)
        {
            "digitallink", "epcis", "masterdata"
        };

        private readonly WireMockServer _server;
        private readonly SqliteConnection _keepAlive;
        private readonly ITraceabilityStore _store;
        private readonly IngestionService _ingestion;
        private readonly DigitalLinkService _digitalLink;
        private readonly EpcisQueryService _epcisQuery;
        private readonly MasterDataService _masterData;
        private readonly string _datasetId;

        private WireMockTraceabilityServer(WireMockServer server, SqliteConnection keepAlive, ITraceabilityStore store,
            string datasetId)
        {
            _server = server;
            _keepAlive = keepAlive;
            _store = store;
            _datasetId = datasetId;
            _ingestion = new IngestionService(store);
            _digitalLink = new DigitalLinkService();
            _epcisQuery = new EpcisQueryService(store);
            _masterData = new MasterDataService(store);
        }

        /// <summary>The base URL of the running WireMock server.</summary>
        public string Url => _server.Url ?? _server.Urls.First();

        /// <summary>
        /// Starts an in-process traceability server backed by an in-memory SQLite database.
        /// </summary>
        public static WireMockTraceabilityServer StartWireMockTraceabilityServer(WireMockTraceabilityConfig config)
        {
            OpenTraceability.Setup.Initialize();
            OpenTraceability.GDST.Setup.Initialize();

            // Shared-cache in-memory SQLite: the keep-alive connection keeps the DB alive while the
            // context factory opens its own connections to the same shared database.
            string connectionString = $"DataSource=wiremock-{Guid.NewGuid():N};Mode=Memory;Cache=Shared";
            var keepAlive = new SqliteConnection(connectionString);
            keepAlive.Open();

            var options = new DbContextOptionsBuilder<TraceabilityDbContext>()
                .UseSqlite(connectionString)
                .Options;
            var factory = new SimpleDbContextFactory(options);

            var store = new TraceabilityStore(factory);
            store.InitializeAsync().GetAwaiter().GetResult();

            // The dataset records are the source of truth for module minification, exactly as on
            // the real server.
            store.UpsertDatasetAsync(new Dataset
            {
                DatasetId = config.DatasetId,
                Modules = ToModuleNames(config.Modules)
            }).GetAwaiter().GetResult();
            foreach (var ds in config.Datasets)
            {
                store.UpsertDatasetAsync(new Dataset
                {
                    DatasetId = ds.DatasetId,
                    Modules = ToModuleNames(ds.Modules)
                }).GetAwaiter().GetResult();
            }

            var server = config.Port.HasValue
                ? WireMockServer.Start(config.Port.Value)
                : WireMockServer.Start();

            var instance = new WireMockTraceabilityServer(server, keepAlive, store, config.DatasetId);

            // Seed data.
            foreach (var doc in config.SeedEpcisDocuments)
            {
                instance.AddEpcisDocument(doc);
            }
            foreach (var md in config.SeedMasterData)
            {
                instance._ingestion.IngestMasterDataAsync(config.DatasetId, md).GetAwaiter().GetResult();
            }
            foreach (var ds in config.Datasets)
            {
                foreach (var doc in ds.SeedEpcisDocuments)
                {
                    var format = doc.TrimStart().StartsWith("<") ? EPCISDataFormat.XML : EPCISDataFormat.JSON;
                    instance._ingestion.IngestEpcisDocumentAsync(ds.DatasetId, doc, format, checkSchema: false).GetAwaiter().GetResult();
                }
                foreach (var md in ds.SeedMasterData)
                {
                    instance._ingestion.IngestMasterDataAsync(ds.DatasetId, md).GetAwaiter().GetResult();
                }
            }

            instance.RegisterRoutes();
            return instance;
        }

        private static List<string> ToModuleNames(IEnumerable<GdstModule>? modules)
            => (modules ?? Enumerable.Empty<GdstModule>())
                .Where(m => m != GdstModule.Core)
                .Distinct()
                .Select(m => m.ToString())
                .ToList();

        // ---- public seeding helpers ----

        /// <summary>Adds events to the in-memory store.</summary>
        public void AddEvents(IEnumerable<IEvent> events)
            => _store.UpsertEventsAsync(_datasetId, events).GetAwaiter().GetResult();

        /// <summary>Adds master data vocabulary elements to the in-memory store.</summary>
        public void AddMasterData(IEnumerable<IVocabularyElement> masterData)
            => _store.UpsertMasterDataAsync(_datasetId, masterData).GetAwaiter().GetResult();

        /// <summary>Ingests a raw EPCIS document (JSON-LD or XML auto-detected). Seed data is trusted,
        /// so schema validation is skipped to keep the in-memory test host fast and offline-friendly.</summary>
        public void AddEpcisDocument(string rawDocument)
        {
            var format = rawDocument.TrimStart().StartsWith("<") ? EPCISDataFormat.XML : EPCISDataFormat.JSON;
            _ingestion.IngestEpcisDocumentAsync(_datasetId, rawDocument, format, checkSchema: false).GetAwaiter().GetResult();
        }

        /// <summary>Ingests raw GS1 Web Vocab master data JSON-LD.</summary>
        public void AddMasterDataJson(string rawMasterData)
            => _ingestion.IngestMasterDataAsync(_datasetId, rawMasterData).GetAwaiter().GetResult();

        // ---- route registration ----

        private void RegisterRoutes()
        {
            // Bare routes (served from the primary dataset) and dataset-prefixed routes, matching
            // the real server's dual route templates. The handlers branch on the first path segment.
            _server.Given(Request.Create().WithPath(new WildcardMatcher("/digitallink/*")).UsingGet())
                   .RespondWith(Response.Create().WithCallback(HandleDigitalLink));
            _server.Given(Request.Create().WithPath(new WildcardMatcher("/*/digitallink/*")).UsingGet())
                   .RespondWith(Response.Create().WithCallback(HandleDigitalLink));

            _server.Given(Request.Create().WithPath("/epcis/events").UsingGet())
                   .RespondWith(Response.Create().WithCallback(HandleEpcisQuery));
            _server.Given(Request.Create().WithPath(new WildcardMatcher("/*/epcis/events")).UsingGet())
                   .RespondWith(Response.Create().WithCallback(HandleEpcisQuery));

            _server.Given(Request.Create().WithPath(new WildcardMatcher("/masterdata/*")).UsingGet())
                   .RespondWith(Response.Create().WithCallback(HandleMasterData));
            _server.Given(Request.Create().WithPath(new WildcardMatcher("/*/masterdata/*")).UsingGet())
                   .RespondWith(Response.Create().WithCallback(HandleMasterData));
        }

        /// <summary>
        /// Splits a request path into its dataset id and the segments after the resource root.
        /// "/epcis/events" → primary dataset, no prefix; "/{ds}/epcis/events" → dataset "ds".
        /// Returns false when the path does not contain <paramref name="root"/> where expected.
        /// </summary>
        private bool TryParsePath(string path, string root, out string datasetId, out string? routeDatasetId, out List<string> segments)
        {
            var parts = path.Trim('/').Split('/').ToList();
            datasetId = _datasetId;
            routeDatasetId = null;
            segments = new List<string>();

            if (parts.Count > 0 && string.Equals(parts[0], root, StringComparison.OrdinalIgnoreCase))
            {
                segments = parts.Skip(1).ToList();
                return true;
            }
            if (parts.Count > 1 && !_knownRoots.Contains(parts[0]) && string.Equals(parts[1], root, StringComparison.OrdinalIgnoreCase))
            {
                datasetId = parts[0];
                routeDatasetId = parts[0];
                segments = parts.Skip(2).ToList();
                return true;
            }
            return false;
        }

        /// <summary>Loads the dataset record, or null when it does not exist (the caller returns 404).</summary>
        private Dataset? GetDataset(string datasetId)
            => _store.GetDatasetAsync(datasetId).GetAwaiter().GetResult();

        private static WireMockResponse UnknownDataset(string datasetId)
            => Json(404, JsonConvert.SerializeObject(new { error = $"Unknown dataset '{datasetId}'." }));

        private WireMockResponse HandleDigitalLink(WireMockRequest request)
        {
            try
            {
                if (!TryParsePath(request.Path, "digitallink", out string datasetId, out string? routeDatasetId, out var segments))
                {
                    return Json(404, JsonConvert.SerializeObject(new { error = "not a digital link path" }));
                }

                var dataset = GetDataset(datasetId);
                if (dataset == null)
                {
                    return UnknownDataset(datasetId);
                }

                string? linkType = GetQuery(request, "linkType");
                string? accept = GetHeader(request, "Accept");
                string? masterDataPath = ResolveMasterDataPath(segments);

                // Mode 1: linkset requested (media type or linkType=linkset) — return it, never redirect.
                bool wantsLinkset = (linkType != null && linkType.ToLower() == DigitalLinkVocab.LinksetLinkType)
                    || AcceptContains(accept, DigitalLinkVocab.LinksetMediaType);
                if (wantsLinkset)
                {
                    string anchor = new Uri(request.Url).GetLeftPart(UriPartial.Path);
                    var linkset = _digitalLink.BuildLinkset(Url, anchor, masterDataPath, routeDatasetId);
                    return Json(200, JsonConvert.SerializeObject(linkset), DigitalLinkVocab.LinksetMediaType);
                }

                // Mode 2: browser-like client — redirect to the requested/default link (404 if absent).
                if (AcceptContains(accept, "text/html"))
                {
                    string? target = _digitalLink.ResolveTargetHref(Url, masterDataPath, linkType, routeDatasetId);
                    if (target == null)
                    {
                        return Json(404, JsonConvert.SerializeObject(new { error = $"no link of type '{linkType}'." }));
                    }
                    return Redirect(target + new Uri(request.Url).Query);
                }

                // Mode 3: legacy flat digital link array (default; keeps 1.1.2 consumers working).
                var links = ResolveDigitalLinks(segments, linkType, routeDatasetId);
                return Json(200, JsonConvert.SerializeObject(links));
            }
            catch (Exception ex)
            {
                return Json(500, JsonConvert.SerializeObject(new { error = ex.Message }));
            }
        }

        /// <summary>
        /// Maps digital link path segments to the relative master data path for that identifier
        /// (e.g. <c>product/{gtin}</c>), or null when the identifier has no master data (SSCC) or the
        /// segments are unrecognized. Mirrors the identifier switch in <see cref="ResolveDigitalLinks"/>.
        /// </summary>
        private static string? ResolveMasterDataPath(List<string> segments)
        {
            if (segments.Count >= 2)
            {
                string key = segments[0].ToLower();
                string id = segments[1];
                switch (key)
                {
                    case "01":
                    case "gtin":
                    case "product":
                        return $"product/{id}";
                    case "414":
                    case "gln":
                    case "location":
                        return $"location/{id}";
                    case "417":
                    case "pgln":
                    case "party":
                        return $"party/{id}";
                    // SSCC (00/sscc) has no master data.
                }
            }

            return null;
        }

        private List<OpenTraceability.Models.MasterData.DigitalLink> ResolveDigitalLinks(List<string> segments, string? linkType, string? routeDatasetId)
        {
            // segments like 01/{gtin}[/10/{lot}|/21/{serial}] or word aliases. Like the real server,
            // only dataset-prefixed requests emit dataset-prefixed links; bare requests emit bare links.
            string baseUrl = Url;

            if (segments.Count >= 2)
            {
                string key = segments[0].ToLower();
                string id = segments[1];
                switch (key)
                {
                    case "01":
                    case "gtin":
                        if (segments.Count >= 4 && (segments[2] == "10" || segments[2].ToLower() == "lot"))
                            return _digitalLink.ForEpcClass(baseUrl, id, segments[3], linkType, routeDatasetId);
                        if (segments.Count >= 4 && (segments[2] == "21" || segments[2].ToLower() == "serial"))
                            return _digitalLink.ForEpcInstance(baseUrl, id, segments[3], linkType, routeDatasetId);
                        return _digitalLink.ForProduct(baseUrl, id, linkType, routeDatasetId);
                    case "00":
                    case "sscc":
                        return _digitalLink.ForSSCC(baseUrl, id, linkType, routeDatasetId);
                    case "414":
                    case "gln":
                    case "location":
                        return _digitalLink.ForLocation(baseUrl, id, linkType, routeDatasetId);
                    case "417":
                    case "pgln":
                    case "party":
                        return _digitalLink.ForParty(baseUrl, id, linkType, routeDatasetId);
                    case "product":
                        return _digitalLink.ForProduct(baseUrl, id, linkType, routeDatasetId);
                }
            }

            return new List<OpenTraceability.Models.MasterData.DigitalLink>();
        }

        private WireMockResponse HandleEpcisQuery(WireMockRequest request)
        {
            try
            {
                if (!TryParsePath(request.Path, "epcis", out string datasetId, out _, out _))
                {
                    return Json(404, JsonConvert.SerializeObject(new { error = "not an epcis path" }));
                }

                var dataset = GetDataset(datasetId);
                if (dataset == null)
                {
                    return UnknownDataset(datasetId);
                }

                var uri = new Uri(request.Url);
                var parameters = new EPCISQueryParameters(uri);
                string json = _epcisQuery.QueryEventsJsonAsync(datasetId, parameters, dataset.GetExpandedModules()).GetAwaiter().GetResult();
                return Json(200, json);
            }
            catch (Exception ex)
            {
                return Json(500, JsonConvert.SerializeObject(new { error = ex.Message }));
            }
        }

        private WireMockResponse HandleMasterData(WireMockRequest request)
        {
            try
            {
                if (!TryParsePath(request.Path, "masterdata", out string datasetId, out _, out var segments))
                {
                    return Json(404, JsonConvert.SerializeObject(new { error = "not a masterdata path" }));
                }

                var dataset = GetDataset(datasetId);
                if (dataset == null)
                {
                    return UnknownDataset(datasetId);
                }

                if (segments.Count < 2)
                {
                    return Json(400, JsonConvert.SerializeObject(new { error = "expected /masterdata/{type}/{identifier}" }));
                }

                string identifier = segments[segments.Count - 1];
                string? json = _masterData.GetMasterDataJsonAsync(datasetId, identifier, dataset.GetExpandedModules()).GetAwaiter().GetResult();
                if (json == null)
                {
                    return Json(404, JsonConvert.SerializeObject(new { error = $"master data not found for {identifier}" }));
                }
                return Json(200, json);
            }
            catch (Exception ex)
            {
                return Json(500, JsonConvert.SerializeObject(new { error = ex.Message }));
            }
        }

        // ---- helpers ----

        private static string? GetQuery(WireMockRequest request, string key)
        {
            if (request.Query != null && request.Query.TryGetValue(key, out var values))
            {
                return values?.FirstOrDefault();
            }
            return null;
        }

        /// <summary>Reads a request header (case-insensitive), joining multiple values with commas.</summary>
        private static string? GetHeader(WireMockRequest request, string key)
        {
            if (request.Headers == null)
            {
                return null;
            }

            foreach (var kvp in request.Headers)
            {
                if (string.Equals(kvp.Key, key, StringComparison.OrdinalIgnoreCase))
                {
                    return string.Join(",", kvp.Value);
                }
            }

            return null;
        }

        /// <summary>Returns true when an Accept header value contains the given media type.</summary>
        private static bool AcceptContains(string? accept, string mediaType)
            => accept != null && accept.IndexOf(mediaType, StringComparison.OrdinalIgnoreCase) >= 0;

        private static WireMockResponse Json(int statusCode, string body, string contentType = "application/json")
        {
            return new WireMockResponse
            {
                StatusCode = statusCode,
                Headers = new Dictionary<string, WireMockList<string>>
                {
                    ["Content-Type"] = new WireMockList<string>(contentType)
                },
                BodyData = new BodyData
                {
                    DetectedBodyType = BodyType.String,
                    BodyAsString = body
                }
            };
        }

        /// <summary>Builds a 302 redirect response pointing at the given absolute location.</summary>
        private static WireMockResponse Redirect(string location)
        {
            return new WireMockResponse
            {
                StatusCode = 302,
                Headers = new Dictionary<string, WireMockList<string>>
                {
                    ["Location"] = new WireMockList<string>(location)
                }
            };
        }

        public void Dispose()
        {
            _server.Stop();
            _server.Dispose();
            _keepAlive.Close();
            _keepAlive.Dispose();
        }

        /// <summary>Minimal IDbContextFactory implementation for the in-memory store.</summary>
        private sealed class SimpleDbContextFactory : IDbContextFactory<TraceabilityDbContext>
        {
            private readonly DbContextOptions<TraceabilityDbContext> _options;
            public SimpleDbContextFactory(DbContextOptions<TraceabilityDbContext> options) => _options = options;
            public TraceabilityDbContext CreateDbContext() => new TraceabilityDbContext(_options);
        }
    }
}
