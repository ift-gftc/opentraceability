using OpenTraceability.Interfaces;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System.Collections.ObjectModel;

namespace OpenTraceability.Models.Events
{
    public class TransformationEvent<T> : EventBase, ITransformationEvent, IILMDEvent<T> where T : EventILMD
    {
        [OpenTraceabilityProducts("inputQuantityList", EventProductType.Input, 8, OpenTraceabilityProductsListType.QuantityList)]
        [OpenTraceabilityProducts("inputEPCList", EventProductType.Input, 7, OpenTraceabilityProductsListType.EPCList)]
        public List<EventProduct> Inputs { get; set; } = new List<EventProduct>();

        [OpenTraceabilityProducts("outputQuantityList", EventProductType.Output, 10, OpenTraceabilityProductsListType.QuantityList)]
        [OpenTraceabilityProducts("outputEPCList", EventProductType.Output, 9, OpenTraceabilityProductsListType.EPCList)]
        public List<EventProduct> Outputs { get; set; } = new List<EventProduct>();

        public EventAction? Action { get; set; }

        [OpenTraceability("transformationID", 11)]
        public string TransformationID { get; set; }

        [OpenTraceability("bizStep", 12)]
        public Uri BusinessStep { get; set; }

        [OpenTraceability("disposition", 13)]
        public Uri Disposition { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability("readPoint", 14)]
        public EventReadPoint ReadPoint { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability("bizLocation", 15)]
        public EventLocation Location { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("bizTransaction")]
        [OpenTraceability("bizTransactionList", 16)]
        public List<EventBusinessTransaction> BizTransactionList { get; set; } = new List<EventBusinessTransaction>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("source")]
        [OpenTraceability("sourceList", 17)]
        public List<EventSource> SourceList { get; set; } = new List<EventSource>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("destination")]
        [OpenTraceability("destinationList", 18)]
        public List<EventDestination> DestinationList { get; set; } = new List<EventDestination>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("sensorElement")]
        [OpenTraceability("sensorElementList", 19)]
        public List<SensorElement> SensorElementList { get; set; } = new List<SensorElement>();

        [OpenTraceabilityObject]
        [OpenTraceability("persistentDisposition", 20)]
        public PersistentDisposition PersistentDisposition { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability("ilmd", 21)]
        public T ILMD { get; set; }

        [OpenTraceabilityXmlIgnore]
        [OpenTraceability("type", 0)]
        public EventType EventType => EventType.TransformationEvent;

        public EventILMD GetILMD() => ILMD;

        public ReadOnlyCollection<EventProduct> Products
        {
            get
            {
                List<EventProduct> products = new List<EventProduct>();
                products.AddRange(Inputs);
                products.AddRange(Outputs);
                return new ReadOnlyCollection<EventProduct>(products);
            }
        }

        public void AddProduct(EventProduct product)
        {
            if (product.Type == EventProductType.Output)
            {
                this.Outputs.Add(product);
            }
            else if (product.Type == EventProductType.Input)
            {
                this.Inputs.Add(product);
            }
            else
            {
                throw new Exception("Transformation event only supports inputs and outputs.");
            }
        }

        public void RemoveProduct(EventProduct product)
        {
            if (product.Type == EventProductType.Output)
            {
                this.Outputs.Remove(product);
            }
            else if (product.Type == EventProductType.Input)
            {
                this.Inputs.Remove(product);
            }
            else
            {
                throw new Exception($"Transformation event only supports inputs and outputs and does not contain this product as either one: {product.EPC}");
            }
        }
    }
}