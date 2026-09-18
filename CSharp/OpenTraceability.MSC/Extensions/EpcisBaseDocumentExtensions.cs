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

        public static List<EPC> GetInputEPCs(this EPCISBaseDocument doc, EPC rootEPC, List<EPC> epcs, int recursionDepth)
        {
            recursionDepth++;
            if (recursionDepth > 15)
            {
                throw new Exception("Recusrion depth exceeded while trying to get relevant EPCs.");
            }

            // make sure the root is added so we always atleast return the root.
            if (!epcs.Contains(rootEPC))
            {
                epcs.Add(rootEPC);
            }

            // go through each event...
            foreach (var evt in doc.Events)
            {
                if (evt.Products.Any(x => x.Type == EventProductType.Output && x.EPC.Equals(rootEPC)))
                {
                    var inputProducts = evt.Products.Where(x => x.Type == EventProductType.Input).ToList();
                    foreach (var input in inputProducts)
                    {
                        if (!epcs.Contains(input.EPC))
                        {
                            epcs.Add(input.EPC);
                            epcs = doc.GetInputEPCs(input.EPC, epcs, recursionDepth);
                        }
                    }
                }
            }

            return epcs;
        }
    }
}
