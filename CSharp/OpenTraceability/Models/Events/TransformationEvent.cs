using OpenTraceability.Interfaces;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;
using System.Collections.ObjectModel;

namespace OpenTraceability.Models.Events
{
    public class TransformationEvent : EventBase, IEvent
    {
        public EventType EventType => EventType.Transformation;
        public string? TransformationID { get; set; }
        public List<EventProduct> Inputs { get; set; } = new List<EventProduct>();
        public List<EventProduct> Outputs { get; set; } = new List<EventProduct>();

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
    }
}