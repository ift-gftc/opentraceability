using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Newtonsoft.Json;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Queries;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.GDST.Modules;
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
    /// in-memory SQLite database using the same core services as the real Docker server.
    /// </summary>
    public sealed class WireMockTraceabilityServer : IDisposable
    {
        private readonly WireMockServer _server;
        private readonly SqliteConnection _keepAlive;
        private readonly ITraceabilityStore _store;
        private readonly IngestionService _ingestion;
        private readonly DigitalLinkService _digitalLink;
        private readonly EpcisQueryService _epcisQuery;
        private readonly MasterDataService _masterData;
        private readonly HashSet<GdstModule> _modules;
        private readonly string _datasetId;

        private WireMockTraceabilityServer(WireMockServer server, SqliteConnection keepAlive, ITraceabilityStore store,
            HashSet<GdstModule> modules, string datasetId)
        {
            _server = server;
            _keepAlive = keepAlive;
            _store = store;
            _modules = modules;
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

            var modules = ModuleSet.Expand(config.Modules);

            var server = config.Port.HasValue
                ? WireMockServer.Start(config.Port.Value)
                : WireMockServer.Start();

            var instance = new WireMockTraceabilityServer(server, keepAlive, store, modules, config.DatasetId);

            // Seed data.
            foreach (var doc in config.SeedEpcisDocuments)
            {
                instance.AddEpcisDocument(doc);
            }
            foreach (var md in config.SeedMasterData)
            {
                instance._ingestion.IngestMasterDataAsync(config.DatasetId, md).GetAwaiter().GetResult();
            }

            instance.RegisterRoutes();
            return instance;
        }

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
            _server.Given(Request.Create().WithPath(new WildcardMatcher("/digitallink/*")).UsingGet())
                   .RespondWith(Response.Create().WithCallback(HandleDigitalLink));

            _server.Given(Request.Create().WithPath("/epcis/events").UsingGet())
                   .RespondWith(Response.Create().WithCallback(HandleEpcisQuery));

            _server.Given(Request.Create().WithPath(new WildcardMatcher("/masterdata/*")).UsingGet())
                   .RespondWith(Response.Create().WithCallback(HandleMasterData));
        }

        private WireMockResponse HandleDigitalLink(WireMockRequest request)
        {
            try
            {
                string? linkType = GetQuery(request, "linkType");
                var links = ResolveDigitalLinks(request.Path, linkType);
                return Json(200, JsonConvert.SerializeObject(links));
            }
            catch (Exception ex)
            {
                return Json(500, JsonConvert.SerializeObject(new { error = ex.Message }));
            }
        }

        private List<OpenTraceability.Models.MasterData.DigitalLink> ResolveDigitalLinks(string path, string? linkType)
        {
            // path like /digitallink/01/{gtin}[/10/{lot}|/21/{serial}] or word aliases
            var segments = path.TrimStart('/').Split('/').Skip(1).ToList(); // drop "digitallink"
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
                            return _digitalLink.ForEpcClass(baseUrl, id, segments[3], linkType);
                        if (segments.Count >= 4 && (segments[2] == "21" || segments[2].ToLower() == "serial"))
                            return _digitalLink.ForEpcInstance(baseUrl, id, segments[3], linkType);
                        return _digitalLink.ForProduct(baseUrl, id, linkType);
                    case "00":
                    case "sscc":
                        return _digitalLink.ForSSCC(baseUrl, id, linkType);
                    case "414":
                    case "gln":
                    case "location":
                        return _digitalLink.ForLocation(baseUrl, id, linkType);
                    case "417":
                    case "pgln":
                    case "party":
                        return _digitalLink.ForParty(baseUrl, id, linkType);
                    case "product":
                        return _digitalLink.ForProduct(baseUrl, id, linkType);
                }
            }

            return new List<OpenTraceability.Models.MasterData.DigitalLink>();
        }

        private WireMockResponse HandleEpcisQuery(WireMockRequest request)
        {
            try
            {
                var uri = new Uri(request.Url);
                var parameters = new EPCISQueryParameters(uri);
                string json = _epcisQuery.QueryEventsJsonAsync(_datasetId, parameters, _modules).GetAwaiter().GetResult();
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
                var segments = request.Path.TrimStart('/').Split('/').Skip(1).ToList(); // drop "masterdata"
                if (segments.Count < 2)
                {
                    return Json(400, JsonConvert.SerializeObject(new { error = "expected /masterdata/{type}/{identifier}" }));
                }

                string identifier = segments[segments.Count - 1];
                string? json = _masterData.GetMasterDataJsonAsync(_datasetId, identifier, _modules).GetAwaiter().GetResult();
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

        private static WireMockResponse Json(int statusCode, string body)
        {
            return new WireMockResponse
            {
                StatusCode = statusCode,
                Headers = new Dictionary<string, WireMockList<string>>
                {
                    ["Content-Type"] = new WireMockList<string>("application/json")
                },
                BodyData = new BodyData
                {
                    DetectedBodyType = BodyType.String,
                    BodyAsString = body
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
