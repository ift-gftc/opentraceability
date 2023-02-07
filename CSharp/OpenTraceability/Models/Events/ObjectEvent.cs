using OpenTraceability.Interfaces;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;
using System.Collections.ObjectModel;

namespace OpenTraceability.Models.Events
{
    public class ObjectEvent : EventBase, IEvent
    {
        public EventType EventType => EventType.Object;
        public List<EventProduct> ReferenceProducts { get; set; } = new List<EventProduct>();

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
    }
}