using OpenTraceability.Interfaces;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    public class SensorMetaData
    {
        [OpenTraceability("@time")]
        public DateTimeOffset? TimeStamp { get; set; }

        [OpenTraceability("@deviceID")]
        public Uri? DeviceID { get; set; }

        [OpenTraceability("@deviceMetadata")]
        public Uri? DeviceMetaData { get; set; }

        [OpenTraceability("@rawData")]
        public Uri? RawData { get; set; }

        [OpenTraceability("@startTime")]
        public DateTimeOffset? StartTime { get; internal set; }

        [OpenTraceability("@bizRules")]
        public Uri? BizRules { get; internal set; }

        [OpenTraceability("@dataProcessingMethod")]
        public Uri? DataProcessingMethod { get; internal set; }

        [OpenTraceability("@endTime")]
        public DateTimeOffset? EndTime { get; internal set; }

        [OpenTraceabilityExtensionAttributes]
        public List<IEventKDE> ExtensionAttributes { get; internal set; } = new List<IEventKDE>();
    }
}