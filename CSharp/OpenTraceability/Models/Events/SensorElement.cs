using OpenTraceability.Interfaces;

namespace OpenTraceability.Models.Events
{
    public class SensorElement
    {
        public SensorMetaData? MetaData { get; set; }
        public List<SensorReport> Reports { get; set; } = new List<SensorReport>();
        public List<IEventKDE> ExtensionKDEs { get; internal set; } = new List<IEventKDE>();
    }
}