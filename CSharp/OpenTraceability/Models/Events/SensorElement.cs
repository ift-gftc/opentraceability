namespace OpenTraceability.Models.Events
{
    public class SensorElement
    {
        public DateTime? TimeStamp { get; set; }
        public Uri? DeviceID { get; set; }
        public string? DeviceMetaData { get; set; }
        public Uri? RawData { get; set; }
        public List<SensorReport> Reports { get; set; } = new List<SensorReport>();
    }
}