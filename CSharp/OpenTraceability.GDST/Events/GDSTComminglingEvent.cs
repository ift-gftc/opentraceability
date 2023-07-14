using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events
{
    public class GDSTComminglingEvent: TransformationEvent<GDSTILMD>, IGDSTILMDEvent   
    {
        [OpenTraceability(Constants.GDST_NAMESPACE, "productOwner")]
        [OpenTraceabilityJson("gdst:productOwner")]
        public PGLN? ProductOwner { get; set; }

        public GDSTComminglingEvent()
        {
            this.BusinessStep = new Uri("urn:gdst:bizStep:commingling");
        }
    }
}
