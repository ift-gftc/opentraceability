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
        /// The modules the server supports. Core is always included; Wildcaught/Aquaculture imply Seafood.
        /// </summary>
        public List<GdstModule> Modules { get; set; } = new List<GdstModule>();

        /// <summary>
        /// Optional fixed port. If null, WireMock chooses a free port.
        /// </summary>
        public int? Port { get; set; }

        /// <summary>
        /// The dataset id used for seeded/added data. Defaults to "default".
        /// </summary>
        public string DatasetId { get; set; } = "default";

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
}
