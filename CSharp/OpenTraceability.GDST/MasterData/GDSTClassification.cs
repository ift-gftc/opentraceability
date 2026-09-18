using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.MasterData
{
    public class GDSTClassification
    {
        [OpenTraceabilityJson("type")]
        [OpenTraceability("type")]
        public string? Type { get; set; }

        [OpenTraceabilityJson("value")]
        [OpenTraceability("value")]
        public string? Value { get; set; }
    }
}
