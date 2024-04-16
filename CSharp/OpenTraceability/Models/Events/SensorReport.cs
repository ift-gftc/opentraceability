using OpenTraceability.Interfaces;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    public class SensorReport
    {
        [OpenTraceabilityJson("time")]
        [OpenTraceability("@time")]
        public DateTimeOffset? TimeStamp { get; set; }

        [OpenTraceabilityJson("type")]
        [OpenTraceability("@type")]
        public Uri Type { get; set; }

        [OpenTraceabilityJson("value")]
        [OpenTraceability("@value")]
        public double? Value { get; internal set; }

        [OpenTraceabilityJson("stringValue")]
        [OpenTraceability("@stringValue")]
        public string StringValue { get; internal set; }

        [OpenTraceabilityJson("booleanValue")]
        [OpenTraceability("@booleanValue")]
        public bool? BooleanValue { get; internal set; }

        [OpenTraceabilityJson("hexBinaryValue")]
        [OpenTraceability("@hexBinaryValue")]
        public string HexBinaryValue { get; internal set; }

        [OpenTraceabilityJson("uriValue")]
        [OpenTraceability("@uriValue")]
        public Uri URIValue { get; internal set; }

        [OpenTraceabilityJson("uom")]
        [OpenTraceability("@uom")]
        public UOM UOM { get; internal set; }

        [OpenTraceabilityJson("component")]
        [OpenTraceability("@component")]
        public Uri Component { get; internal set; }

        [OpenTraceabilityJson("minValue")]
        [OpenTraceability("@minValue")]
        public double? MinValue { get; internal set; }

        [OpenTraceabilityJson("maxValue")]
        [OpenTraceability("@maxValue")]
        public double? MaxValue { get; internal set; }

        [OpenTraceabilityJson("sDev")]
        [OpenTraceability("@sDev")]
        public double? SDev { get; internal set; }

        [OpenTraceabilityJson("chemicalSubstance")]
        [OpenTraceability("@chemicalSubstance")]
        public Uri ChemicalSubstance { get; internal set; }

        [OpenTraceabilityJson("microorganism")]
        [OpenTraceability("@microorganism")]
        public Uri MicroOrganism { get; internal set; }

        [OpenTraceabilityJson("deviceID")]
        [OpenTraceability("@deviceID")]
        public Uri DeviceID { get; internal set; }

        [OpenTraceabilityJson("rawData")]
        [OpenTraceability("@rawData")]
        public Uri RawData { get; internal set; }

        [OpenTraceabilityJson("meanValue")]
        [OpenTraceability("@meanValue")]
        public double? MeanValue { get; internal set; }

        [OpenTraceabilityJson("percRank")]
        [OpenTraceability("@percRank")]
        public double? PercentageRank { get; internal set; }

        [OpenTraceabilityJson("dataProcessingMethod")]
        [OpenTraceability("@dataProcessingMethod")]
        public Uri DataProcessingMethod { get; internal set; }

        [OpenTraceabilityJson("coordinateReferenceSystem")]
        [OpenTraceability("@coordinateReferenceSystem")]
        public Uri CoordinateReferenceSystem { get; internal set; }

        [OpenTraceabilityJson("exception")]
        [OpenTraceability("@exception")]
        public Uri Exception { get; internal set; }

        [OpenTraceabilityJson("percValue")]
        [OpenTraceability("@percValue")]
        public double? PercentageValue { get; internal set; }

        [OpenTraceabilityJson("deviceMetadata")]
        [OpenTraceability("@deviceMetadata")]
        public Uri DeviceMetadata { get; internal set; }

        [OpenTraceabilityExtensionAttributes]
        public List<IEventKDE> ExtensionAttributes { get; internal set; } = new List<IEventKDE>();
    }
}