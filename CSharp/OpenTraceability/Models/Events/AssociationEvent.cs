using OpenTraceability.Interfaces;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System.Collections.Generic;
using System.Collections.ObjectModel;

namespace OpenTraceability.Models.Events
{
    public class AssociationEvent : EventBase, IEvent
    {
        [OpenTraceability("parentID", 7)]
        public EPC? ParentID { get; set; }

        [OpenTraceabilityProducts("childQuantityList", EventProductType.Child, 9, OpenTraceabilityProductsListType.QuantityList)]
        [OpenTraceabilityProducts("childEPCs", EventProductType.Child, 8, OpenTraceabilityProductsListType.EPCList)]
        public List<EventProduct> Children { get; set; } = new List<EventProduct>();

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
        [OpenTraceability("bizTransactionList", 15)]
        public List<EventBusinessTransaction> BizTransactionList { get; set; } = new List<EventBusinessTransaction>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("source")]
        [OpenTraceability("sourceList", 16)]
        public List<EventSource> SourceList { get; set; } = new List<EventSource>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("destination")]
        [OpenTraceability("destinationList", 17)]
        public List<EventDestination> DestinationList { get; set; } = new List<EventDestination>();

        [OpenTraceabilityObject]
        [OpenTraceabilityArray("sensorElement")]
        [OpenTraceability("sensorElementList", 18)]
        public List<SensorElement> SensorElementList { get; set; } = new List<SensorElement>();

        [OpenTraceabilityObject]
        [OpenTraceability("persistentDisposition", 19)]
        public PersistentDisposition? PersistentDisposition { get; set; }

        public EventILMD? ILMD { get => throw new Exception("AssociationEvent does not support ILMD."); set => throw new Exception("AssociationEvent does not support ILMD."); }

        public EventType EventType => EventType.Association;

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
                throw new Exception("Association event only supports children and parents.");
            }
        }
    }
}