using OpenTraceability.Interfaces;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    public class SensorReport
    {
        [OpenTraceabilityXml("@time")]
        public DateTimeOffset? TimeStamp { get; set; }

        [OpenTraceabilityXml("@type")]
        public Uri? Type { get; set; }

        [OpenTraceabilityXml("@value")]
        public double? Value { get; internal set; }

        [OpenTraceabilityXml("@stringValue")]
        public string? StringValue { get; internal set; }

        [OpenTraceabilityXml("@booleanValue")]
        public bool? BooleanValue { get; internal set; }

        [OpenTraceabilityXml("@hexBinaryValue")]
        public string? HexBinaryValue { get; internal set; }

        [OpenTraceabilityXml("@uriValue")]
        public Uri? URIValue { get; internal set; }

        [OpenTraceabilityXml("@uom")]
        public UOM? UOM { get; internal set; }

        [OpenTraceabilityXml("@component")]
        public Uri? Component { get; internal set; }

        [OpenTraceabilityXml("@minValue")]
        public double? MinValue { get; internal set; }

        [OpenTraceabilityXml("@maxValue")]
        public double? MaxValue { get; internal set; }

        [OpenTraceabilityXml("@sDev")]
        public double? SDev { get; internal set; }

        [OpenTraceabilityXml("@chemicalSubstance")]
        public Uri? ChemicalSubstance { get; internal set; }

        [OpenTraceabilityXml("@microorganism")]
        public Uri? MicroOrganism { get; internal set; }

        [OpenTraceabilityXml("@deviceID")]
        public Uri? DeviceID { get; internal set; }

        [OpenTraceabilityXml("@rawData")]
        public Uri? RawData { get; internal set; }

        [OpenTraceabilityXml("@meanValue")]
        public double? MeanValue { get; internal set; }

        [OpenTraceabilityXml("@percRank")]
        public double? PercentageRank { get; internal set; }

        [OpenTraceabilityXml("@dataProcessingMethod")]
        public Uri? DataProcessingMethod { get; internal set; }

        [OpenTraceabilityXml("@coordinateReferenceSystem")]
        public Uri? CoordinateReferenceSystem { get; internal set; }

        [OpenTraceabilityXml("@exception")]
        public Uri? Exception { get; internal set; }

        [OpenTraceabilityXml("@percValue")]
        public double? PercentageValue { get; internal set; }

        [OpenTraceabilityXml("@deviceMetadata")]
        public Uri? DeviceMetadata { get; internal set; }

        [OpenTraceabilityExtensionAttributes]
        public List<IEventKDE> ExtensionAttributes { get; internal set; } = new List<IEventKDE>();
    }
}