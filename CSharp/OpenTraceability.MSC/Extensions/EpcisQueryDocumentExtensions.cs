using OpenTraceability.GDST.Events;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;

namespace OpenTraceability.MSC.Extensions
{
    public static class EpcisQueryDocumentExtensions
    {
        public static Dictionary<string, EPCISQueryDocument> SplitByGLN(this EPCISQueryDocument doc)
        {
            Dictionary<string, EPCISQueryDocument> docs = new Dictionary<string, EPCISQueryDocument>();
            List<IEvent> aggregationEvents = doc.Events.Where(x => x is GDSTAggregationEvent).ToList();
            foreach (var evt in doc.Events)
            {
                GLN? gln = evt.Location?.GLN;
                string glnRaw = gln?.ToString() ?? "";
                if (!string.IsNullOrEmpty(glnRaw))
                {
                    if (!docs.ContainsKey(glnRaw))
                    {
                        var newDoc = new EPCISQueryDocument();
                        newDoc.EPCISVersion = EPCISVersion.V2;
                        newDoc.CreationDate = DateTimeOffset.UtcNow;
                        newDoc.MasterData = doc.MasterData.ToList();
                        newDoc.Events = new List<IEvent>();
                        docs.Add(glnRaw, newDoc);
                    }
                    docs[glnRaw].Events.Add(evt);
                }
            }

            // always include aggregation events in the split doc to support resolving SSCCs and aggregated products during analysis and review.
            foreach (var kvp in docs)
            {
                kvp.Value.Events.AddRange(aggregationEvents);
            }

            return docs;
        }

        public static List<IEvent> GetEvents_rec(this EPCISBaseDocument doc, EPC epc)
        {
            List<IEvent> events = new List<IEvent>();

            // for each event, replace any aggregated products with their children. 
            foreach (var e in doc.Events.Where(x => x.EventType != EventType.AggregationEvent).ToList())
            {
                foreach (var p in e.Products)
                {
                    bool isAnAggregatedProduct = doc.Events.Any(x => x.Products.Any(y => y.EPC.Equals(p.EPC) && y.Type == EventProductType.Parent));
                    if (isAnAggregatedProduct)
                    {
                        var childProducts = doc.TraceBackChildren(p.EPC, e);
                        e.RemoveProduct(p);
                        foreach (var child in childProducts)
                        {
                            e.AddProduct(new(child.EPC)
                            {
                                Type = EventProductType.Reference,
                                Quantity = child.Quantity
                            });
                        }
                    }
                }
            }

            // find all events where this EPC is an output, reference, or parent
            foreach (var e in doc.Events)
            {
                foreach (var p in e.Products.Where(p => p.Type == EventProductType.Output || p.Type == EventProductType.Reference || p.Type == EventProductType.Parent))
                {
                    if (p.EPC.Equals(epc))
                    {
                        events.Add(e);
                        break;
                    }
                }
            }

            // foreach event we found, go through and find all EPCs that are children or inputs
            foreach (var e in events.ToList())
            {
                foreach (var p in e.Products.Where(p => p.Type == EventProductType.Input || p.Type == EventProductType.Child))
                {
                    List<IEvent> input_events = doc.GetEvents_rec(p.EPC);
                    foreach (var e2 in input_events)
                    {
                        if (!events.Exists(e => e.EventID == e2.EventID))
                        {
                            events.Add(e2);
                        }
                    }
                }
            }

            return events;
        }

        public static List<EventProduct> TraceBackChildren(this EPCISBaseDocument doc, EPC? parentEPC, IEvent currentEvent)
        {
            if (parentEPC == null) return new List<EventProduct>();

            List<EventProduct> allChildren = new();

            IEvent? mostRecentAggregationEvent = doc.Events.Where(x => x.Products.Any(y => y.EPC == parentEPC && y.Type == EventProductType.Parent) && x.EventTime < currentEvent.EventTime).OrderByDescending(x => x.EventTime).FirstOrDefault();
            if (mostRecentAggregationEvent != null)
            {
                List<EPC> childEPCs = mostRecentAggregationEvent.Products.Where(x => x.Type == EventProductType.Child).Select(x => x.EPC).ToList();
                foreach (EPC childEPC in childEPCs)
                {
                    allChildren = doc.TraceBackChildren(childEPC, mostRecentAggregationEvent);
                }
            }
            else
            {
                allChildren = currentEvent.Products.Where(x => x.Type == EventProductType.Child).ToList();
            }

            return allChildren;
        }

        public static EPCISQueryDocument GetProductEvents(this EPCISBaseDocument doc, EPC epc)
        {
            var isolatedDoc = new EPCISQueryDocument();
            isolatedDoc.MasterData = doc.MasterData.ToList();
            isolatedDoc.Events = doc.GetEvents_rec(epc);

            return isolatedDoc;
        }

        public static List<EPC> GetChildEPCs(this EPCISQueryDocument doc, EPC sscc)
        {
            List<EPC> childEPCs = new();

            // get the most recent aggregation event for this SSCC
            GDSTAggregationEvent? aggregationEvent = doc.Events.OrderByDescending(x => x.EventTime).FirstOrDefault(x => x.EventType == EventType.AggregationEvent && x.Products.Any(y => y.EPC == sscc)) as GDSTAggregationEvent;
            if (aggregationEvent == null)
            {
                return childEPCs;
            }

            childEPCs = aggregationEvent.Products.Where(x => x.Type == EventProductType.Child).Select(x => x.EPC).ToList();

            List<EPC> aggregatedChildren = childEPCs.Where(x => x.Type == EPCType.SSCC).ToList();
            foreach (EPC child in aggregatedChildren)
            {
                childEPCs.Remove(child);
                childEPCs.AddRange(doc.GetChildEPCs(child));
            }

            return childEPCs;
        }
    }
}
