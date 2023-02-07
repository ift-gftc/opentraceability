using OpenTraceability.Interfaces;
using OpenTraceability.Models.Identifiers;
using System.Collections.ObjectModel;

namespace OpenTraceability.Models.Events
{
    public class TransactionEvent : EventBase, IEvent
    {
        public EPC? ParentID { get; set; }
        public EventType EventType => EventType.Transaction;
        public List<EventProduct> ReferenceProducts { get; set; } = new List<EventProduct>();

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