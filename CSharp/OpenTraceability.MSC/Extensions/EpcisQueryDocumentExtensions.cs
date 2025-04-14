using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;

namespace OpenTraceability.MSC.Extensions
{
    public static class EpcisQueryDocumentExtensions
    {
        public static List<IEvent> GetEvents_rec(this EPCISQueryDocument doc, EPC epc)
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

        public static List<EventProduct> TraceBackChildren(this EPCISQueryDocument doc, EPC? parentEPC, IEvent currentEvent)
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

        public static EPCISQueryDocument GetProductEvents(this EPCISQueryDocument doc, EPC epc)
        {
            // there is a bug here somehow, and we need to re-write this...
            var isolatedDoc = new EPCISQueryDocument();
            isolatedDoc.MasterData = doc.MasterData.ToList();
            isolatedDoc.Events = doc.GetEvents_rec(epc);

            return isolatedDoc;
        }
    }
}
