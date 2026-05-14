using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.MasterData
{
    public class GDSTClassification
    {
        [OpenTraceabilityJson("type")]
        public string? Type { get; set; }

        [OpenTraceabilityJson("value")]
        public string? Value { get; set; }
    }
}
