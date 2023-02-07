using OpenTraceability.Interfaces;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;
using System.Collections.Generic;
using System.Collections.ObjectModel;

namespace OpenTraceability.Models.Events
{
    public class AssociationEvent : EventBase, IEvent
    {
        public EPC? ParentID { get; set; }
        public List<EventProduct> Children { get; set; } = new List<EventProduct>();
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