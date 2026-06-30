using System.Collections.Generic;

namespace OpenTraceability.TestServer.Core.Models
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
}
