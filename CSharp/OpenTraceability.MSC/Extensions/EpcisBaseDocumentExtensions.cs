using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;

namespace OpenTraceability.MSC.Extensions
{
    public static class EpcisBaseDocumentExtensions
    {
        public static IEvent? GetFirstEventWithProduct(this EPCISBaseDocument doc, string epc)
        {
            IEvent? firstEventWithProduct = doc?.Events?.FirstOrDefault(e => e.Products.Any(x => x.EPC?.ToString()?.Equals(epc) == true));
            return firstEventWithProduct;
        }

        public static IEvent? GetFirstEventWithProductDefinition(this EPCISBaseDocument doc, string? gtin)
        {
            if(string.IsNullOrEmpty(gtin))
            {
                return null;
            }

            IEvent? firstEventWithProduct = doc?.Events?.FirstOrDefault(e => e.Products.Any(x => x.EPC?.GTIN?.ToString()?.Equals(gtin) == true));
            return firstEventWithProduct;
        }

        public static IEvent? GetFirstEventWithProductDefinition(this EPCISBaseDocument doc, GTIN? gtin)
        {
            return doc.GetFirstEventWithProductDefinition(gtin?.ToString());
        }
    }
}
