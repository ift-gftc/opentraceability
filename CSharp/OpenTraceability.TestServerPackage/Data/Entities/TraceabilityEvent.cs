using System;
using System.ComponentModel.DataAnnotations;

namespace OpenTraceability.TestServer.Core.Data.Entities
{
    /// <summary>
    /// The full record of a single EPCIS event, stored as a single-event EPCIS Query Document
    /// in JSON-LD. This is the source of truth that gets re-serialized (and minified) on query.
    /// </summary>
    public class TraceabilityEvent
    {
        [Key]
        public long Id { get; set; }

        /// <summary>
        /// The dataset (a.k.a. blob) this event belongs to. Allows a single server instance to
        /// host multiple isolated datasets. Defaults to "default".
        /// </summary>
        public string DatasetId { get; set; } = "default";

        /// <summary>
        /// The EPCIS event ID (UUID).
        /// </summary>
        public string EventId { get; set; } = string.Empty;

        /// <summary>
        /// The full single-event EPCIS Query Document serialized as JSON-LD.
        /// </summary>
        public string EventJson { get; set; } = string.Empty;

        /// <summary>
        /// The business step (lowercased) for quick filtering / reporting.
        /// </summary>
        public string BizStep { get; set; } = string.Empty;

        /// <summary>
        /// The action (lowercased) for quick filtering / reporting.
        /// </summary>
        public string Action { get; set; } = string.Empty;

        public DateTimeOffset? EventTime { get; set; }

        public DateTime RecordTime { get; set; } = DateTime.UtcNow;
    }
}
