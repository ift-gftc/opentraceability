using OpenTraceability.Interfaces;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    public class SensorMetaData
    {
        [OpenTraceabilityXml("@time")]
        public DateTimeOffset? TimeStamp { get; set; }

        [OpenTraceabilityXml("@deviceID")]
        public Uri? DeviceID { get; set; }

        [OpenTraceabilityXml("@deviceMetadata")]
        public Uri? DeviceMetaData { get; set; }

        [OpenTraceabilityXml("@rawData")]
        public Uri? RawData { get; set; }

        [OpenTraceabilityXml("@startTime")]
        public DateTimeOffset? StartTime { get; internal set; }

        [OpenTraceabilityXml("@bizRules")]
        public Uri? BizRules { get; internal set; }

        [OpenTraceabilityXml("@dataProcessingMethod")]
        public Uri? DataProcessingMethod { get; internal set; }

        [OpenTraceabilityXml("@endTime")]
        public DateTimeOffset? EndTime { get; internal set; }

        [OpenTraceabilityExtensionAttributes]
        public List<IEventKDE> ExtensionAttributes { get; internal set; } = new List<IEventKDE>();
    }
}