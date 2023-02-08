using OpenTraceability.Interfaces;
using OpenTraceability.Utility;

namespace OpenTraceability.Models.Events
{
    public class SensorReport
    {
        public DateTime? TimeStamp { get; set; }
        public Uri? Type { get; set; }
        public Measurement? Measurement { get; set; }
        public double? Value { get; internal set; }
        public string? StringValue { get; internal set; }
        public bool? BooleanValue { get; internal set; }
        public string? HexBinaryValue { get; internal set; }
        public Uri? URIValue { get; internal set; }
        public UOM? UOM { get; internal set; }
        public Uri? Component { get; internal set; }
        public double? MinValue { get; internal set; }
        public double? MaxValue { get; internal set; }
        public double? SDev { get; internal set; }
        public Uri? ChemicalSubstance { get; internal set; }
        public Uri? MicroOrganism { get; internal set; }
        public Uri? DeviceID { get; internal set; }
        public Uri? RawData { get; internal set; }
        public double? MeanValue { get; internal set; }
        public double? PercentageRank { get; internal set; }
        public Uri? DataProcessingMethod { get; internal set; }
        public Uri? CoordinateReferenceSystem { get; internal set; }
        public Uri? Exception { get; internal set; }
        public double? PercentageValue { get; internal set; }
        public Uri? DeviceMetadata { get; internal set; }
        public List<IEventKDE> ExtensionAttributes { get; internal set; } = new List<IEventKDE>();
    }
}