using OpenTraceability.Interfaces;

namespace OpenTraceability.Models.Events
{
    public class SensorMetaData
    {
        public DateTime? TimeStamp { get; set; }
        public Uri? DeviceID { get; set; }
        public Uri? DeviceMetaData { get; set; }
        public Uri? RawData { get; set; }
        public List<SensorReport> Reports { get; set; } = new List<SensorReport>();
        public DateTime? StartTime { get; internal set; }
        public Uri? BizRules { get; internal set; }
        public Uri? DataProcessingMethod { get; internal set; }
        public DateTime? EndTime { get; internal set; }
        public List<IEventKDE> ExtensionAttributes { get; internal set; } = new List<IEventKDE>();
    }
}