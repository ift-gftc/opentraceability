using OpenTraceability.Interfaces;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    [OpenTraceabilityXml("sensorElement")]
    public class SensorElement
    {
        [OpenTraceabilityObject]
        [OpenTraceabilityXml("sensorMetadata", 0)]
        public SensorMetaData? MetaData { get; set; }

        [OpenTraceabilityArrayAttribute]
        [OpenTraceabilityXml("sensorReport", 1)]
        public List<SensorReport> Reports { get; set; } = new List<SensorReport>();

        [OpenTraceabilityExtensionElements]
        public List<IEventKDE> ExtensionKDEs { get; internal set; } = new List<IEventKDE>();
    }
}