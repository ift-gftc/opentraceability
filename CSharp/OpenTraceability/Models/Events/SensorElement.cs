using OpenTraceability.Interfaces;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    public class SensorElement
    {
        [OpenTraceabilityObject]
        [OpenTraceability("sensorMetadata", 0)]
        public SensorMetaData MetaData { get; set; }

        [OpenTraceabilityArray]
        [OpenTraceabilityObject]
        [OpenTraceability("sensorReport", 1)]
        public List<SensorReport> Reports { get; set; } = new List<SensorReport>();

        [OpenTraceabilityExtensionElements]
        public List<IEventKDE> ExtensionKDEs { get; internal set; } = new List<IEventKDE>();
    }
}