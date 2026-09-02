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

        /// <summary>
        /// The GLN of the event's business location (lowercased), or empty when the event has none.
        /// Backs the EQ_bizLocation query parameter; source/destination GLNs are deliberately excluded.
        /// </summary>
        public string BizLocationGLN { get; set; } = string.Empty;

        /// <summary>
        /// The transformation ID (lowercased) when the event is a transformation event, otherwise empty.
        /// Backs the EQ_transformationID query parameter.
        /// </summary>
        public string TransformationId { get; set; } = string.Empty;

        /// <summary>
        /// The event time normalized to UTC. Stored as a DateTime because the SQLite EF provider
        /// cannot translate DateTimeOffset comparisons to SQL.
        /// </summary>
        public DateTime? EventTime { get; set; }

        public DateTime RecordTime { get; set; } = DateTime.UtcNow;
    }
}
