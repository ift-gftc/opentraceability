using OpenTraceability.Utility;

namespace OpenTraceability.Models.Events
{
    public class SensorReport
    {
        public DateTime? TimeStamp { get; set; }
        public Uri? Type { get; set; }
        public Measurement? Measurement { get; set; }
    }
}