using OpenTraceability.GDST.Events;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.MSC.Events
{
    /// <summary>
    /// A farm harvest event as defined by GDST 1.2: TRANSFORMATION / business step "urn:gdst:bizStep:farmHarvest".
    /// GDST 2.0 expresses it as a transformation event whose output is classified as mature.
    /// </summary>
    public class GDSTFarmHarvestEvent : TransformationEvent<GDSTILMD>, IGDSTEvent
    {
        [OpenTraceability(Constants.GDST_NAMESPACE, "productOwner")]
        [OpenTraceabilityJson("gdst:productOwner")]
        public PGLN? ProductOwner { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "humanWelfarePolicy")]
        [OpenTraceabilityJson("gdst:humanWelfarePolicy")]
        public string? HumanWelfarePolicy { get; set; }

        public GDSTFarmHarvestEvent()
        {
            this.BusinessStep = new Uri("urn:gdst:bizStep:farmHarvest");
        }
    }
}
