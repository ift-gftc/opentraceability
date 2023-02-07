using OpenTraceability.Models.Identifiers;

namespace OpenTraceability.Models.Events
{
    public class EventProduct
    {
        public EPC? EPC { get; set; }
        public Measurement Quantity { get; set; }
        public EventProductType Type { get; set; }
    }
}