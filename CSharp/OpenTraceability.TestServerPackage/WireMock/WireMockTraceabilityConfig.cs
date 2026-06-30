using OpenTraceability.GDST;

namespace OpenTraceability.TestServer.Core.WireMock
{
    /// <summary>
    /// Configuration for an in-process WireMock traceability server used in external .NET test
    /// projects. The server behaves like the real Docker server (digital link, EPCIS query, master
    /// data) but uses an in-memory SQLite database. It does not implement traceback or capability tests.
    /// </summary>
    public class WireMockTraceabilityConfig
    {
        /// <summary>
        /// The modules of the primary dataset (<see cref="DatasetId"/>). Core is always included;
        /// Wildcaught/Aquaculture imply Seafood.
        /// </summary>
        public List<GdstModule> Modules { get; set; } = new List<GdstModule>();

        /// <summary>
        /// Optional fixed port. If null, WireMock chooses a free port.
        /// </summary>
        public int? Port { get; set; }

        /// <summary>
        /// The dataset id used for seeded/added data and for requests that do not carry a dataset
        /// path prefix. Defaults to "default".
        /// </summary>
        public string DatasetId { get; set; } = "default";

        /// <summary>
        /// Additional datasets to create, each with its own module set. They are served via the
        /// dataset-prefixed routes (/{datasetId}/digitallink/..., /{datasetId}/epcis/events,
        /// /{datasetId}/masterdata/...), matching the real server, so one WireMock host can serve
        /// multiple module tiers side by side. Requests for datasets that were not configured
        /// return 404, matching the real server's strict behavior.
        /// </summary>
        public List<WireMockDataset> Datasets { get; set; } = new List<WireMockDataset>();

        /// <summary>
        /// Optional EPCIS documents (JSON-LD or XML strings) to seed at startup. The format is
        /// auto-detected by leading character ('{' or '[' = JSON, otherwise XML).
        /// </summary>
        public List<string> SeedEpcisDocuments { get; set; } = new List<string>();

        /// <summary>
        /// Optional GS1 Web Vocab master data documents (JSON-LD strings) to seed at startup.
        /// </summary>
        public List<string> SeedMasterData { get; set; } = new List<string>();
    }

    /// <summary>An additional dataset hosted by the WireMock server.</summary>
    public class WireMockDataset
    {
        public string DatasetId { get; set; } = string.Empty;

        /// <summary>The dataset's modules. Core is always included; Wildcaught/Aquaculture imply Seafood.</summary>
        public List<GdstModule> Modules { get; set; } = new List<GdstModule>();

        /// <summary>Optional EPCIS documents (JSON-LD or XML strings) to seed into this dataset at startup.</summary>
        public List<string> SeedEpcisDocuments { get; set; } = new List<string>();

        /// <summary>Optional GS1 Web Vocab master data documents (JSON-LD strings) to seed into this dataset.</summary>
        public List<string> SeedMasterData { get; set; } = new List<string>();
    }
}
