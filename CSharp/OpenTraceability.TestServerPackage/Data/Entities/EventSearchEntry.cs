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

        public string ProductGTIN { get; set; } = string.Empty;

        public string LocationGLN { get; set; } = string.Empty;

        public string PartyPGLN { get; set; } = string.Empty;

        /// <summary>
        /// Builds the denormalized search rows for a batch of events. Mirrors the extraction
        /// approach used by the TraceabilityDriver so identifier matching behaves consistently.
        /// </summary>
        public static List<EventSearchEntry> CreateSearchEntries(string datasetId, IEnumerable<IEvent> events)
        {
            var entries = new List<EventSearchEntry>();

            foreach (IEvent evt in events)
            {
                string bizStep = evt.BusinessStep?.ToString().ToLower() ?? string.Empty;
                string action = evt.Action.ToString()?.ToLower() ?? string.Empty;
                DateTimeOffset? eventTime = evt.EventTime;
                DateTime recordTime = evt.RecordTime?.UtcDateTime ?? DateTime.UtcNow;

                var epcs = evt.Products.Select(p => p.EPC.ToString().ToLower()).ToList();
                var productGTINs = evt.Products.Select(p => p.EPC.GTIN?.ToString().ToLower())
                                               .Where(g => g != null).Select(g => g!).ToList();
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

                int maxCount = Math.Max(Math.Max(Math.Max(epcs.Count, productGTINs.Count), locationGLNs.Count), partyPGLNs.Count);
                maxCount = Math.Max(maxCount, 1); // always emit at least one row so the event is discoverable by time/bizStep

                for (int i = 0; i < maxCount; i++)
                {
                    entries.Add(new EventSearchEntry
                    {
                        DatasetId = datasetId,
                        EventId = evt.EventID?.ToString() ?? throw new Exception("The event has no EventID; events must have an EventID stamped before they are indexed."),
                        BizStep = bizStep,
                        Action = action,
                        EventTime = eventTime,
                        RecordTime = recordTime,
                        EPC = i < epcs.Count ? epcs[i] : string.Empty,
                        ProductGTIN = i < productGTINs.Count ? productGTINs[i] : string.Empty,
                        LocationGLN = i < locationGLNs.Count ? locationGLNs[i] : string.Empty,
                        PartyPGLN = i < partyPGLNs.Count ? partyPGLNs[i] : string.Empty
                    });
                }
            }

            return entries;
        }
    }
}
