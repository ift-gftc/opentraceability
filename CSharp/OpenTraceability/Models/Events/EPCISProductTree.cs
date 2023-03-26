using OpenTraceability.Interfaces;
using OpenTraceability.Models.Identifiers;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Models.Events
{
    /// <summary>
    /// Represents a product-tree where each node is an EPC, and list of input EPCs, and
    /// a list of associated events with that EPC. This tool works well when all the events
    /// in the traceability data have a location (GLN) set on them. Otherwise, it can
    /// produce strange results.
    /// </summary>
    public class EPCISProductTree
    {
        /// <summary>
        /// The EPC of this node.
        /// </summary>
        public List<EPC> EPCs { get; set; } = new List<EPC>();

        /// <summary>
        /// The GLN of the location for where this EPC is in the supply chain.
        /// </summary>
        public GLN? GLN { get; set; }

        /// <summary>
        /// The source nodes in the supply chain.
        /// </summary>

        public List<EPCISProductTree> SourceNodes { get; set; } = new List<EPCISProductTree>();

        /// <summary>
        /// All events associated with this EPC, and source EPCs that occured at this location.
        /// </summary>
        public List<IEvent> Events { get; set; } = new List<IEvent>();


        public EPCISProductTree()
        {

        }

        public EPCISProductTree(List<EPC> epcs, GLN gln, EPCISBaseDocument doc)
        {
            this.EPCs = epcs;
            this.GLN = gln;
            Build(doc);
        }

        /// <summary>
        /// In order to build our supply chain, we are going to take the following steps:
        /// -	Find all our internal events.
        /// -	Foreach EPC…
        ///         o Find source products.
        /// -	Foreach Source Product…
        ///         o Find where source product came from…
        /// -	Create groups of source products and source locations, grouping by source location.
        ///         o Create source node from each group.
        /// </summary>
        /// <param name="doc">The traceability data we are analyzing.</param>
        private void Build(EPCISBaseDocument doc)
        {
            // find the internal events
            FindInternalEvents(doc);

            List<EPC> sourceEPCs = new List<EPC>();

            // foreach epc, find the source products
            // here we keep looping until we are getting back the same results we put in
            foreach (var epc in this.EPCs)
            {
                var sProducts = FindMySourceProducts(epc, doc, out bool foundSourceProducts);
                var temp = sProducts;

                int stack = 0;
                while (foundSourceProducts && stack < 50)
                {
                    sProducts = new List<EPC>();
                    foreach (var e in temp)
                    {
                        var r = FindMySourceProducts(e, doc, out bool found);
                        foundSourceProducts &= found;
                        sProducts.AddRange(r);
                    }
                    temp = sProducts;
                    stack++;
                }
                sourceEPCs.AddRange(sProducts);
            }

            // remove any duplicates
            sourceEPCs = sourceEPCs.Distinct().ToList();

            // find source locations
            List<KeyValuePair<GLN, EPC>> sourceLocationsRaw = new List<KeyValuePair<GLN, EPC>>();
            foreach (var epc in sourceEPCs)
            {
                foreach (var sourceLocation in FindSourceLocationsForSourceProduct(epc, doc))
                {
                    sourceLocationsRaw.Add(new KeyValuePair<GLN, EPC>(sourceLocation, epc));
                }
            }

            // group by source location
            foreach (var group in sourceLocationsRaw.GroupBy(s => s.Key))
            {
                var epcs = group.Select(g => g.Value).ToList();

                EPCISProductTree treeNode = new EPCISProductTree(epcs, group.Key, doc);
                this.SourceNodes.Add(treeNode);
            }
        }

        /// <summary>
        /// Find all events where one of my EPCs is listed as anything except a child and
        /// matches the GLN of this node.
        /// </summary>
        private void FindInternalEvents(EPCISBaseDocument doc)
        {
            this.Events = doc.Events.Where(e => e.Products.Any(p => this.EPCs.Contains(p.EPC) && p.Type != EventProductType.Child)).ToList();
        }

        /// <summary>
        /// We are trying to find the products that were received at this location in relation to the
        /// products that left it, defined in the EPCs property. "internal events" below refer to the
        /// events in the Events property.
        /// 
        /// 1.	Finding Input Products via TRANSFORMATION internal events.
        ///     a.First look for a TRANSFORMATION internal events where I am in the outputs.
        ///     b.Next, look for all TRANSFORMATION internal events with the same Transformation ID.
        ///         i.Take each input product across all these events as the source products.
        /// 
        /// 2.	Finding Parent ID of AGGREGATION – DELETE internal events where I am the child.
        ///     a.First look for AGGREGATION – DELETE where I am listed as a child in the internal events.
        ///         i.Take the Parent ID as the source product.
        ///     b.Or look for the most recent AGGREGATION – ADD in the internal events where I am last
        ///       as a child, and the find the next AGGREGATION – DELETE event with the same parent.
        /// 
        /// 3.	Finding children of AGGREGATION – ADD events where I am the parent.
        /// 
        /// 4.	If we find no source products, then we take this EPC as the source product.
        /// </summary>
        /// <param name="epc"></param>
        /// <param name="doc"></param>
        /// <returns></returns>
        private List<EPC> FindMySourceProducts(EPC epc, EPCISBaseDocument doc, out bool foundSourceProducts)
        {
            foundSourceProducts = true;
            var transformEvent = this.Events.FirstOrDefault(e => e.Products.Any(p => p.EPC == epc && p.Type == EventProductType.Output)) as ITransformationEvent;
            if (transformEvent != null)
            {
                // find all linked transformation events
                if (!string.IsNullOrWhiteSpace(transformEvent.TransformationID))
                {
                    var events = this.Events.Where(e => (e as ITransformationEvent)?.TransformationID == transformEvent.TransformationID);

                    List<EPC> sourceEPCs = events.SelectMany(e => e.Products)
                                                 .Where(p => p.Type == EventProductType.Input)
                                                 .Select(p => p.EPC).ToList();

                    return sourceEPCs;
                }
                else
                {
                    List<EPC> sourceEPCs = transformEvent.Products.Where(p => p.Type == EventProductType.Input)
                                                                  .Select(p => p.EPC).ToList();
                    return sourceEPCs;
                }
            }
            else
            {
                // find children of AGGREGATION - ADD where I am the parent
                var aggEvent = this.Events.FirstOrDefault(e => e is IAggregationEvent && e.Action == EventAction.ADD && e.Products.Any(p => epc == p.EPC && p.Type == EventProductType.Parent)) as IAggregationEvent;
                if (aggEvent != null)
                {
                    List<EPC> sourceEPCs = aggEvent.Products.Where(p => p.Type == EventProductType.Input)
                                                            .Select(p => p.EPC).ToList();
                    return sourceEPCs;
                }
                else
                {
                    // find latest AGGREGATION - DELETE events where I am the child...
                    var allDisaggregationEvents = this.Events.Where(e => e is IAggregationEvent && e.Action == EventAction.DELETE && e.Products.Any(p => epc == p.EPC && p.Type == EventProductType.Child));
                    if (allDisaggregationEvents.Count() > 0)
                    {
                        var disAggEvent = (IAggregationEvent)allDisaggregationEvents.OrderByDescending(e => e.EventTime).First();
                        return new List<EPC> { disAggEvent.ParentID };
                    }
                    // hail mary, we will look for AGGREGATION - ADD events where I am listed, that occured prior to any events in my internal events
                    else
                    {
                        var minEventTime = this.Events.Select(e => e.EventTime).Min();
                        var aggEvents = doc.Events.Where(e => e.EventTime < minEventTime && e is IAggregationEvent && e.Action == EventAction.ADD && e.Products.Any(p => epc == p.EPC && p.Type == EventProductType.Child))
                                                  .Select(e => (IAggregationEvent)e);

                        if (aggEvents.Count() > 0)
                        {
                            aggEvent = aggEvents.OrderByDescending(a => a.EventTime).First();

                            // now lets see if a dis-aggregation event took place in our internal events for this parent
                            var disAggEvent = this.Events.FirstOrDefault(e => e is IAggregationEvent && e.Action == EventAction.DELETE && e.Products.Any(p => epc == p.EPC && p.Type == EventProductType.Parent)) as IAggregationEvent;
                            if (disAggEvent != null)
                            {
                                return new List<EPC> { disAggEvent.ParentID };
                            }
                        }
                    }
                }
            }

            // if we could not find any source products, then it is its own source product
            foundSourceProducts = false;
            return new List<EPC> { epc };
        }

        /// <summary>
        /// This method focuses on finding the source-location(s) of a source-product.
        /// 
        /// 1.	Try and find an event for this source product where the current location is listed as
        ///     the destination location and has a source location that is either in our list of internal
        ///     events or occurred before the earliest event in our internal events.
        ///     
        ///     a.In this case, we might find multiple source locations for the product.
        ///     b.Favor the events in our internal events before the events in the complete list that occurred
        ///       before our internal events.
        ///
        /// 2.	Otherwise, we need to find the earliest event that occurred prior to the earliest event in our internal events.
        /// </summary>
        /// <param name="sourceEPC"></param>
        /// <returns></returns>
        private List<GLN> FindSourceLocationsForSourceProduct(EPC sourceEPC, EPCISBaseDocument doc)
        {
            var minEventTime = this.Events.Select(e => e.EventTime).Min();

            var evts = this.Events.Where(e => e.Products.Any(p => p.Type == EventProductType.Reference && p.EPC == sourceEPC))
                                  .Where(e => e.DestinationList.Any(d => d.ParsedType == EventDestinationType.Location && d.Value?.ToLower() == this.GLN?.ToString()));
            var glns = evts.Select(e => e.Location?.GLN).Where(e => e != null).ToList();
            if (glns.Any())
            {
#pragma warning disable CS8619 // Nullability of reference types in value doesn't match target type.
                return glns;
#pragma warning restore CS8619 // Nullability of reference types in value doesn't match target type.
            }
            else
            {
                // try and find the same in the external events
                evts = doc.Events.Where(e => e.EventTime < minEventTime && e.Products.Any(p => p.Type == EventProductType.Reference && p.EPC == sourceEPC))
                                     .Where(e => e.DestinationList.Any(d => d.ParsedType == EventDestinationType.Location && d.Value?.ToLower() == this.GLN?.ToString()));
                glns = evts.Select(e => e.Location?.GLN).Where(e => e != null).ToList();
                if (glns.Any())
                {
#pragma warning disable CS8619 // Nullability of reference types in value doesn't match target type.
                    return glns;
#pragma warning restore CS8619 // Nullability of reference types in value doesn't match target type.
                }
                else
                {
                    // else find the first event that occured before our earliest internal event
                    var previousEvent = doc.Events.Where(e => e.Products.Any(p => p.Type == EventProductType.Reference && p.EPC == sourceEPC))
                                                  .Where(e => e.EventTime < minEventTime)
                                                  .Where(e => e.Location?.GLN != this.GLN)
                                                  .OrderByDescending(e => e.EventTime)
                                                  .FirstOrDefault();

                    if (previousEvent?.Location?.GLN != null)
                    {
                        return new List<GLN>() { previousEvent.Location.GLN };
                    }
                    else
                    {
                        // we could not find any previous location, so lets just return an empty list
                        return new List<GLN>();
                    }
                }
            }
        }
    }
}
