using OpenTraceability.Models.MasterData;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.MasterData
{
    public class GDSTTradeItem : Tradeitem
    {
        [OpenTraceabilityArray]
        [OpenTraceabilityObject]
        [OpenTraceabilityJson("gdst:productClassification")]
        [OpenTraceabilityMasterData("urn:gdst:kde#productClassification")]
        public List<GDSTClassification> ProductClassification { get; set; } = new List<GDSTClassification>();
    }
}
