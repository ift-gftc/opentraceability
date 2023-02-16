using OpenTraceability.Interfaces;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;
using System.Collections.ObjectModel;

namespace OpenTraceability.Models.Events
{
    public class TransactionEvent : EventBase, IEvent
    {
        [OpenTraceability("parentID", 8)]
        public EPC? ParentID { get; set; }

        [OpenTraceabilityProducts("extension/quantityList", EPCISVersion.V1, EventProductType.Child, 20, OpenTraceabilityProductsListType.QuantityList)]
        [OpenTraceabilityProducts("quantityList", EPCISVersion.V2, EventProductType.Child, 15, OpenTraceabilityProductsListType.QuantityList)]
        [OpenTraceabilityProducts("epcList", EventProductType.Child, 9, OpenTraceabilityProductsListType.EPCList)]
        public List<EventProduct> ReferenceProducts { get; set; } = new List<EventProduct>();

        [OpenTraceability("action", 10)]
        public EventAction? Action { get; set; }

        [OpenTraceability("bizStep", 11)]
        public Uri? BusinessStep { get; set; }

        [OpenTraceability("disposition", 12)]
        public Uri? Disposition { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability("readPoint", 13)]
        public EventReadPoint? ReadPoint { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability("bizLocation", 14)]
        public EventLocation? Location { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("bizTransaction")]
        [OpenTraceability("bizTransactionList", 7)]
        public List<EventBusinessTransaction> BizTransactionList { get; set; } = new List<EventBusinessTransaction>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("source")]
        [OpenTraceability("sourceList", 16, EPCISVersion.V2)]
        [OpenTraceability("baseExtension/sourceList", 21, EPCISVersion.V1)]
        public List<EventSource> SourceList { get; set; } = new List<EventSource>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("destination")]
        [OpenTraceability("destinationList", 17, EPCISVersion.V2)]
        [OpenTraceability("baseExtension/destinationList", 22, EPCISVersion.V1)]
        public List<EventDestination> DestinationList { get; set; } = new List<EventDestination>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("sensorElement")]
        [OpenTraceability("sensorElementList", 18, EPCISVersion.V1)]
        public List<SensorElement> SensorElementList { get; set; } = new List<SensorElement>();

        [OpenTraceabilityObject]
        [OpenTraceability("persistentDisposition", 19, EPCISVersion.V2)]
        public PersistentDisposition? PersistentDisposition { get; set; }

        public EventILMD? ILMD { get => throw new Exception("TransactionEvent does not support ILMD."); set => throw new Exception("TransactionEvent does not support ILMD."); }

        public EventType EventType => EventType.Transaction;

        public ReadOnlyCollection<EventProduct> Products
        {
            get
            {
                List<EventProduct> products = new List<EventProduct>();
                products.Add(new EventProduct()
                {
                    EPC = this.ParentID,
                    Type = EventProductType.Parent
                });
                products.AddRange(this.ReferenceProducts);
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
            else if (product.Type == EventProductType.Reference)
            {
                this.ReferenceProducts.Add(product);
            }
            else
            {
                throw new Exception("Transaction event only supports references and parents.");
            }
        }
    }
}