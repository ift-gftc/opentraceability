using OpenTraceability.Interfaces;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;

namespace OpenTraceability.Models.Events
{
    public class ObjectEvent<T> : EventBase, IILMDEvent<T> where T : EventILMD
    {

        [OpenTraceabilityXmlIgnore]
        [OpenTraceability("type", 0)]
        public EventType EventType => EventType.ObjectEvent;

        [OpenTraceabilityProducts("extension/quantityList", EPCISVersion.V1, EventProductType.Reference, 20, OpenTraceabilityProductsListType.QuantityList)]
        [OpenTraceabilityProducts("quantityList", EPCISVersion.V2, EventProductType.Reference, 14, OpenTraceabilityProductsListType.QuantityList)]
        [OpenTraceabilityProducts("epcList", EventProductType.Reference, 7, OpenTraceabilityProductsListType.EPCList, Required = true)]
        public List<EventProduct> ReferenceProducts { get; set; } = new List<EventProduct>();

        [OpenTraceability("action", 8)]
        public EventAction? Action { get; set; }

        [OpenTraceability("bizStep", 9)]
        public Uri BusinessStep { get; set; }

        [OpenTraceability("disposition", 10)]
        public Uri Disposition { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability("readPoint", 11)]
        public EventReadPoint ReadPoint { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability("bizLocation", 12)]
        public EventLocation Location { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("bizTransaction")]
        [OpenTraceability("bizTransactionList", 13)]
        public List<EventBusinessTransaction> BizTransactionList { get; set; } = new List<EventBusinessTransaction>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("source")]
        [OpenTraceability("sourceList", 15, EPCISVersion.V2)]
        [OpenTraceability("extension/sourceList", 21, EPCISVersion.V1)]
        public List<EventSource> SourceList { get; set; } = new List<EventSource>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("destination")]
        [OpenTraceability("destinationList", 16, EPCISVersion.V2)]
        [OpenTraceability("extension/destinationList", 22, EPCISVersion.V1)]
        public List<EventDestination> DestinationList { get; set; } = new List<EventDestination>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("sensorElement")]
        [OpenTraceability("sensorElementList", 17, EPCISVersion.V2)]
        [OpenTraceability("extension/sensorElementList", EPCISVersion.V1)]
        public List<SensorElement> SensorElementList { get; set; } = new List<SensorElement>();

        [OpenTraceabilityObject]
        [OpenTraceability("persistentDisposition", 18)]
        [OpenTraceability("extension/persistentDisposition", EPCISVersion.V1)]
        public PersistentDisposition PersistentDisposition { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability("ilmd", 19, EPCISVersion.V2)]
        [OpenTraceability("extension/ilmd", 23, EPCISVersion.V1)]
        public T ILMD { get; set; }

        public EventILMD GetILMD() => ILMD;

        public ReadOnlyCollection<EventProduct> Products
        {
            get
            {
                List<EventProduct> products = new List<EventProduct>();
                products.AddRange(this.ReferenceProducts);
                return new ReadOnlyCollection<EventProduct>(products);
            }
        }

        public void AddProduct(EventProduct product)
        {
            if (product.Type == EventProductType.Reference)
            {
                this.ReferenceProducts.Add(product);
            }
            else
            {
                throw new Exception("Object event only supports references.");
            }
        }

        public void RemoveProduct(EventProduct product)
        {
            if (product.Type == EventProductType.Reference)
            {
                this.ReferenceProducts.Remove(product);
            }
            else
            {
                throw new Exception($"Object event only supports references and does not contain this product as a reference: {product.EPC}");
            }
        }
    }
}