using OpenTraceability.Models.Common;
using OpenTraceability.Models.Identifiers;

namespace OpenTraceability.Models.Events
{
    public enum EPCISVersion
    {
        Version_1_2 = 0,
        Version_2_0 = 1
    }

    public class EPCISBaseDocument
    {
        /// <summary>
        /// Represents a list of namespaces used in the document.
        /// </summary>
        public Dictionary<string, string> Namespaces { get; set; } = new Dictionary<string, string>();

        /// <summary>
        /// The version of EPCIS used for this document.
        /// </summary>
        public EPCISVersion EPCISVersion { get; set; }

        private Dictionary<string, EventBase> _eventIDs = new Dictionary<string, EventBase>();
        private List<TransformationEvent> _transformEvents = new List<TransformationEvent>();
        private Dictionary<string, List<TransformationEvent>> _longRunningTransformations = new Dictionary<string, List<TransformationEvent>>();
        private List<AggregationEvent> _aggEvents = new List<AggregationEvent>();
        private Dictionary<EPC, List<EventBase>> _epc_toEvents = new Dictionary<EPC, List<EventBase>>();
        private Dictionary<EPC, EPCISBaseDocument_ProductRelationship> _epc_toRelationships = new Dictionary<EPC, EPCISBaseDocument_ProductRelationship>();

        public StandardBusinessDocumentHeader Header { get; set; }
        public List<EventBase> Events { get; set; } = new List<EventBase>();
        public List<IProduct> ProductDefinitions { get; set; } = new List<IProduct>();
        public List<ILocation> Locations { get; set; } = new List<ILocation>();
        public List<ITradingParty> TradingParties { get; set; } = new List<ITradingParty>();

        /// <summary>
        /// This will re-index the document for faster searching and manipulation.
        /// </summary>
        public void IndexDocument()
        {
            _transformEvents = new List<TransformationEvent>();
            _aggEvents = new List<AggregationEvent>();
            _eventIDs = new Dictionary<string, EventBase>();
            _epc_toEvents = new Dictionary<EPC, List<EventBase>>();
            _epc_toRelationships = new Dictionary<EPC, EPCISBaseDocument_ProductRelationship>();
            _longRunningTransformations = new Dictionary<string, List<TransformationEvent>>();

            // build basic dictionaries
            foreach (var evt in this.Events)
            {
                if (!_eventIDs.ContainsKey(evt.EventID))
                {
                    _eventIDs.Add(evt.EventID, evt);
                }

                if (evt is TransformationEvent)
                {
                    var transformEvent = evt as TransformationEvent;
                    _transformEvents.Add(transformEvent);

                    // build long running transformation event dictionary
                    if (!string.IsNullOrWhiteSpace(transformEvent.TransformationID))
                    {
                        if (!_longRunningTransformations.ContainsKey(transformEvent.TransformationID))
                        {
                            _longRunningTransformations.Add(transformEvent.TransformationID, new List<TransformationEvent>());
                        }
                        _longRunningTransformations[transformEvent.TransformationID].Add(transformEvent);
                    }
                }

                if (evt is AggregationEvent)
                {
                    _aggEvents.Add(evt as AggregationEvent);
                }

                foreach (var p in evt.Products)
                {
                    if (!_epc_toEvents.ContainsKey(p.EPC))
                    {
                        _epc_toEvents.Add(p.EPC, new List<EventBase>());
                    }
                    _epc_toEvents[p.EPC].Add(evt);
                }
            }

            // transformation events relationships
            foreach (var transformEvt in _transformEvents)
            {
                foreach (var output in transformEvt.Products.Where(p => p.Type == EventProductType.Output))
                {
                    foreach (var input in transformEvt.Products.Where(p => p.Type == EventProductType.Input))
                    {
                        if (!_epc_toRelationships.ContainsKey(output.EPC))
                        {
                            _epc_toRelationships.Add(output.EPC, new EPCISBaseDocument_ProductRelationship(output.EPC));
                        }

                        if (!_epc_toRelationships.ContainsKey(input.EPC))
                        {
                            _epc_toRelationships.Add(input.EPC, new EPCISBaseDocument_ProductRelationship(input.EPC));
                        }

                        _epc_toRelationships[output.EPC].Children.Add(_epc_toRelationships[input.EPC]);
                    }

                    if (!string.IsNullOrWhiteSpace(transformEvt.TransformationID) && _longRunningTransformations.ContainsKey(transformEvt.TransformationID))
                    {
                        foreach (var inputEvt in _longRunningTransformations[transformEvt.TransformationID])
                        {
                            foreach (var input in inputEvt.Products.Where(p => p.Type == EventProductType.Input))
                            {
                                if (!_epc_toRelationships.ContainsKey(output.EPC))
                                {
                                    _epc_toRelationships.Add(output.EPC, new EPCISBaseDocument_ProductRelationship(output.EPC));
                                }

                                if (!_epc_toRelationships.ContainsKey(input.EPC))
                                {
                                    _epc_toRelationships.Add(input.EPC, new EPCISBaseDocument_ProductRelationship(input.EPC));
                                }

                                _epc_toRelationships[output.EPC].Children.Add(_epc_toRelationships[input.EPC]);
                            }
                        }
                    }
                }
            }

            // aggregation events relationships
            foreach (var aggEvt in _aggEvents.Where(e => e.Action == EventAction.ADD))
            {
                foreach (var parent in aggEvt.Products.Where(p => p.Type == EventProductType.Parent))
                {
                    foreach (var child in aggEvt.Products.Where(p => p.Type == EventProductType.Child))
                    {
                        if (!_epc_toRelationships.ContainsKey(parent.EPC))
                        {
                            _epc_toRelationships.Add(parent.EPC, new EPCISBaseDocument_ProductRelationship(parent.EPC));
                        }

                        if (!_epc_toRelationships.ContainsKey(child.EPC))
                        {
                            _epc_toRelationships.Add(child.EPC, new EPCISBaseDocument_ProductRelationship(child.EPC));
                        }

                        _epc_toRelationships[parent.EPC].Children.Add(_epc_toRelationships[child.EPC]);
                    }
                }
            }
        }

        public bool EventExists(string eventID)
        {
            return _eventIDs.ContainsKey(eventID);
        }

        protected void IsolateProductTree_Internal(IEPCISBaseDocument newlyConstructedIsolatedDoc, EPC product)
        {
            try
            {
                newlyConstructedIsolatedDoc.Header = this.Header;
                newlyConstructedIsolatedDoc.ProductDefinitions = this.ProductDefinitions;
                newlyConstructedIsolatedDoc.Locations = this.Locations;
                newlyConstructedIsolatedDoc.TradingParties = this.TradingParties;

                List<string> eventIDs = new List<string>();
                List<EPC> products = TraceProductTree(product);
                foreach (EPC epc in products.OrderBy(p => p.ToString()))
                {
                    // grab all events for product
                    foreach (IEvent evt in _epc_toEvents[epc].OrderBy(e => e.EventID))
                    {
                        if (!eventIDs.Contains(evt.EventID))
                        {
                            var clonedEvent = ReflectionCloner.Clone(evt);

                            foreach (var p in clonedEvent.Products.ToList().OrderBy(p => p.EPC.ToString() + ":" + p.Type))
                            {
                                if (!products.Contains(p.EPC))
                                {
                                    clonedEvent.Products.Remove(p);
                                }
                            }

                            newlyConstructedIsolatedDoc.Events.Add(ReflectionCloner.Clone(clonedEvent));
                            eventIDs.Add(evt.EventID);
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                throw new Exception("Error occured during the isolate product tree. Most likely occured because you forgot to index the document after changing it.", ex);
            }
        }

        private List<EPC> TraceProductTree(EPC currentEPC, List<EPC>? epcs = null)
        {
            if (epcs == null)
            {
                epcs = new List<EPC>();
            }
            epcs.Add(currentEPC);

            if (_epc_toRelationships.ContainsKey(currentEPC))
            {
                foreach (var child in _epc_toRelationships[currentEPC].Children)
                {
                    var traceEPCs = TraceProductTree(child.Parent, epcs);
                    epcs.AddRange(traceEPCs);
                }
            }

            return epcs.Distinct().ToList();
        }
    }

    internal class EPCISBaseDocument_ProductRelationship
    {
        public EPCISBaseDocument_ProductRelationship(EPC parent)
        {
            this.Parent = parent;
        }

        internal EPC Parent { get; set; }
        internal List<EPCISBaseDocument_ProductRelationship> Children { get; set; } = new List<EPCISBaseDocument_ProductRelationship>();
    }
}