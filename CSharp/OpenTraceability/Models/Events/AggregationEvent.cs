using OpenTraceability.Interfaces;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System.Collections.Generic;
using System.Collections.ObjectModel;

namespace OpenTraceability.Models.Events
{
    public class AggregationEvent<T> : EventBase, IAggregationEvent, IILMDEvent<T> where T : EventILMD
    {
        [OpenTraceability("parentID", 7)]
        public EPC? ParentID { get; set; }

        [OpenTraceabilityProducts("extension/childQuantityList", EPCISVersion.V1, EventProductType.Child, 21, OpenTraceabilityProductsListType.QuantityList)]
        [OpenTraceabilityProducts("childQuantityList", EPCISVersion.V2, EventProductType.Child, 15, OpenTraceabilityProductsListType.QuantityList)]
        [OpenTraceabilityProducts("childEPCs", EventProductType.Child, 8, OpenTraceabilityProductsListType.EPCList, Required = true)]
        public List<EventProduct> Children { get; set; } = new List<EventProduct>();

        [OpenTraceability("action", 9)]
        public EventAction? Action { get; set; }

        [OpenTraceability("bizStep", 10)]
        public Uri? BusinessStep { get; set; }

        [OpenTraceability("disposition", 11)]
        public Uri? Disposition { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability("readPoint", 12)]
        public EventReadPoint? ReadPoint { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability("bizLocation", 13)]
        public EventLocation? Location { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("bizTransaction")]
        [OpenTraceability("bizTransactionList", 14)]
        public List<EventBusinessTransaction> BizTransactionList { get; set; } = new List<EventBusinessTransaction>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("source")]
        [OpenTraceability("sourceList", 16, EPCISVersion.V2)]
        [OpenTraceability("baseExtension/sourceList", 22, EPCISVersion.V1)]
        public List<EventSource> SourceList { get; set; } = new List<EventSource>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("destination")]
        [OpenTraceability("destinationList", 17, EPCISVersion.V2)]
        [OpenTraceability("baseExtension/destinationList", 23, EPCISVersion.V1)]
        public List<EventDestination> DestinationList { get; set; } = new List<EventDestination>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("sensorElement")]
        [OpenTraceability("sensorElementList", 18, EPCISVersion.V2)]
        public List<SensorElement> SensorElementList { get; set; } = new List<SensorElement>();

        [OpenTraceabilityObject]
        [OpenTraceability("persistentDisposition", 19, EPCISVersion.V2)]
        public PersistentDisposition? PersistentDisposition { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability("ilmd", 20, EPCISVersion.V2)]
        public T? ILMD { get; set; }

        [OpenTraceabilityXmlIgnore]
        [OpenTraceability("type", 0)]
        public EventType EventType => EventType.AggregationEvent;

        public EventILMD? GetILMD() => ILMD;

        public ReadOnlyCollection<EventProduct> Products
        {
            get
            {
                List<EventProduct> products = new List<EventProduct>();
                if (this.ParentID != null)
                {
                    products.Add(new EventProduct(this.ParentID)
                    {
                        Type = EventProductType.Parent
                    });
                }
                products.AddRange(this.Children);
                return new ReadOnlyCollection<EventProduct>(products);
            }
        }

        public void AddProduct(EventProduct product)
        {
            if (product.Type == EventProductType.Parent)
            {
                if (product.Quantity != null)
                {
                    throw new Exception("Parents do not support quantity.");
                }
                this.ParentID = product.EPC;
            }
            else if (product.Type == EventProductType.Child)
            {
                this.Children.Add(product);
            }
            else
            {
                throw new Exception("Aggregation event only supports children and parents.");
            }
        }

        public void RemoveProduct(EventProduct product)
        {
            if (product.Type == EventProductType.Parent)
            {
                this.ParentID = null;
            }
            else if (product.Type == EventProductType.Child)
            {
                this.Children.Remove(product);
            }
            else
            {
                throw new Exception($"Aggregation event only supports children and parents and does not contain this product as either one: {product.EPC}");
            }
        }
    }
}