using OpenTraceability.Interfaces;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    public class SensorReport
    {
        [OpenTraceability("@time")]
        public DateTimeOffset? TimeStamp { get; set; }

        [OpenTraceability("@type")]
        public Uri? Type { get; set; }

        [OpenTraceability("@value")]
        public double? Value { get; internal set; }

        [OpenTraceability("@stringValue")]
        public string? StringValue { get; internal set; }

        [OpenTraceability("@booleanValue")]
        public bool? BooleanValue { get; internal set; }

        [OpenTraceability("@hexBinaryValue")]
        public string? HexBinaryValue { get; internal set; }

        [OpenTraceability("@uriValue")]
        public Uri? URIValue { get; internal set; }

        [OpenTraceability("@uom")]
        public UOM? UOM { get; internal set; }

        [OpenTraceability("@component")]
        public Uri? Component { get; internal set; }

        [OpenTraceability("@minValue")]
        public double? MinValue { get; internal set; }

        [OpenTraceability("@maxValue")]
        public double? MaxValue { get; internal set; }

        [OpenTraceability("@sDev")]
        public double? SDev { get; internal set; }

        [OpenTraceability("@chemicalSubstance")]
        public Uri? ChemicalSubstance { get; internal set; }

        [OpenTraceability("@microorganism")]
        public Uri? MicroOrganism { get; internal set; }

        [OpenTraceability("@deviceID")]
        public Uri? DeviceID { get; internal set; }

        [OpenTraceability("@rawData")]
        public Uri? RawData { get; internal set; }

        [OpenTraceability("@meanValue")]
        public double? MeanValue { get; internal set; }

        [OpenTraceability("@percRank")]
        public double? PercentageRank { get; internal set; }

        [OpenTraceability("@dataProcessingMethod")]
        public Uri? DataProcessingMethod { get; internal set; }

        [OpenTraceability("@coordinateReferenceSystem")]
        public Uri? CoordinateReferenceSystem { get; internal set; }

        [OpenTraceability("@exception")]
        public Uri? Exception { get; internal set; }

        [OpenTraceability("@percValue")]
        public double? PercentageValue { get; internal set; }

        [OpenTraceability("@deviceMetadata")]
        public Uri? DeviceMetadata { get; internal set; }

        [OpenTraceabilityExtensionAttributes]
        public List<IEventKDE> ExtensionAttributes { get; internal set; } = new List<IEventKDE>();
    }
}