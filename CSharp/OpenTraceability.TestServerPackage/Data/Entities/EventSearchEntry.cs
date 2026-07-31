using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using OpenTraceability.GDST.Events;
using OpenTraceability.Interfaces;

namespace OpenTraceability.TestServer.Core.Data.Entities
{
    /// <summary>
    /// A denormalized, indexable search row. One row is created per identifier occurrence
    /// (EPC / GTIN / GLN / PGLN) on an event so that the high-cardinality identifier columns
    /// can be indexed and used to quickly narrow candidate events during a query.
    /// </summary>
    public class EventSearchEntry
    {
        [Key]
        public long Id { get; set; }

        public string DatasetId { get; set; } = "default";

        public string EventId { get; set; } = string.Empty;

        public string BizStep { get; set; } = string.Empty;

        public string Action { get; set; } = string.Empty;

        public DateTimeOffset? EventTime { get; set; }

        public DateTime RecordTime { get; set; } = DateTime.UtcNow;

        public string EPC { get; set; } = string.Empty;

        /// <summary>
        /// The <see cref="OpenTraceability.Models.Events.EventProductType"/> of the product this row's
        /// EPC belongs to (lowercased, e.g. "reference", "child", "input"), or empty for rows that
        /// carry a location/party identifier instead of a product.
        /// </summary>
        public string EpcType { get; set; } = string.Empty;

        public string ProductGTIN { get; set; } = string.Empty;

        public string LocationGLN { get; set; } = string.Empty;

        public string PartyPGLN { get; set; } = string.Empty;

        /// <summary>
        /// Builds the denormalized search rows for a batch of events. Mirrors the extraction
        /// approach used by the TraceabilityDriver so identifier matching behaves consistently.
        /// </summary>
        /// <remarks>
        /// Each row carries exactly one identifier: one row per product (EPC + its own GTIN + its
        /// product type, kept aligned so type-restricted MATCH queries can be answered in SQL), one
        /// row per location GLN, and one row per party PGLN. Events with no identifiers emit no
        /// rows; time/bizStep filtering is served by the Events table, not this one.
        /// </remarks>
        public static List<EventSearchEntry> CreateSearchEntries(string datasetId, IEnumerable<IEvent> events)
        {
            var entries = new List<EventSearchEntry>();

            foreach (IEvent evt in events)
            {
                string eventId = evt.EventID?.ToString() ?? throw new Exception("The event has no EventID; events must have an EventID stamped before they are indexed.");
                string bizStep = evt.BusinessStep?.ToString().ToLower() ?? string.Empty;
                string action = evt.Action.ToString()?.ToLower() ?? string.Empty;
                DateTimeOffset? eventTime = evt.EventTime;
                DateTime recordTime = evt.RecordTime?.UtcDateTime ?? DateTime.UtcNow;

                EventSearchEntry NewRow()
                {
                    return new EventSearchEntry
                    {
                        DatasetId = datasetId,
                        EventId = eventId,
                        BizStep = bizStep,
                        Action = action,
                        EventTime = eventTime,
                        RecordTime = recordTime
                    };
                }

                // One row per product, with the product's EPC, GTIN, and type kept together on the
                // same row so type-restricted EPC/GTIN matching stays answerable in SQL.
                foreach (var product in evt.Products)
                {
                    var row = NewRow();
                    row.EPC = product.EPC.ToString().ToLower();
                    row.EpcType = product.Type.ToString().ToLower();
                    row.ProductGTIN = product.EPC.GTIN?.ToString().ToLower() ?? string.Empty;
                    entries.Add(row);
                }

                // Collect location GLNs: the event's business location plus any SDT_Location
                // sources/destinations. Party PGLNs come from the GDST information provider /
                // product owner and any SDT_Possessor / SDT_Owner sources/destinations.
                var locationGLNs = evt.Location?.GLN != null
                    ? new List<string> { evt.Location.GLN.ToString().ToLower() }
                    : new List<string>();
                var partyPGLNs = new List<string>();

                if (evt is IGDSTEvent gdstEvent && gdstEvent.InformationProvider != null)
                {
                    partyPGLNs.Add(gdstEvent.InformationProvider.ToString().ToLower());
                }
                if (evt is IGDSTProductOwnerEvent ownerEvent && ownerEvent.ProductOwner != null)
                {
                    partyPGLNs.Add(ownerEvent.ProductOwner.ToString().ToLower());
                }

                foreach (var source in evt.SourceList)
                {
                    if (string.IsNullOrWhiteSpace(source.Value)) continue;
                    if (source.Type == OpenTraceability.Constants.EPCIS.URN.SDT_Possessor ||
                        source.Type == OpenTraceability.Constants.EPCIS.URN.SDT_Owner)
                        partyPGLNs.Add(source.Value.ToLower());
                    else if (source.Type == OpenTraceability.Constants.EPCIS.URN.SDT_Location)
                        locationGLNs.Add(source.Value.ToLower());
                }

                foreach (var dest in evt.DestinationList)
                {
                    if (string.IsNullOrWhiteSpace(dest.Value)) continue;
                    if (dest.Type == OpenTraceability.Constants.EPCIS.URN.SDT_Possessor ||
                        dest.Type == OpenTraceability.Constants.EPCIS.URN.SDT_Owner)
                        partyPGLNs.Add(dest.Value.ToLower());
                    else if (dest.Type == OpenTraceability.Constants.EPCIS.URN.SDT_Location)
                        locationGLNs.Add(dest.Value.ToLower());
                }

                foreach (string gln in locationGLNs.Distinct())
                {
                    var row = NewRow();
                    row.LocationGLN = gln;
                    entries.Add(row);
                }

                foreach (string pgln in partyPGLNs.Distinct())
                {
                    var row = NewRow();
                    row.PartyPGLN = pgln;
                    entries.Add(row);
                }
            }

            return entries;
        }
    }
}
