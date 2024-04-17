using OpenTraceability.Interfaces;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;

namespace OpenTraceability.Models.Events
{
    public class SensorMetaData
    {
        [OpenTraceabilityJson("time")]
        [OpenTraceability("@time")]
        public DateTimeOffset? TimeStamp { get; set; }

        [OpenTraceabilityJson("deviceID")]
        [OpenTraceability("@deviceID")]
        public Uri DeviceID { get; set; }

        [OpenTraceabilityJson("deviceMetadata")]
        [OpenTraceability("@deviceMetadata")]
        public Uri DeviceMetaData { get; set; }

        [OpenTraceabilityJson("rawData")]
        [OpenTraceability("@rawData")]
        public Uri RawData { get; set; }

        [OpenTraceabilityJson("startTime")]
        [OpenTraceability("@startTime")]
        public DateTimeOffset? StartTime { get; internal set; }

        [OpenTraceabilityJson("bizRules")]
        [OpenTraceability("@bizRules")]
        public Uri BizRules { get; internal set; }

        [OpenTraceabilityJson("dataProcessingMethod")]
        [OpenTraceability("@dataProcessingMethod")]
        public Uri DataProcessingMethod { get; internal set; }

        [OpenTraceabilityJson("endTime")]
        [OpenTraceability("@endTime")]
        public DateTimeOffset? EndTime { get; internal set; }

        [OpenTraceabilityExtensionAttributes]
        public List<IEventKDE> ExtensionAttributes { get; internal set; } = new List<IEventKDE>();
    }
}