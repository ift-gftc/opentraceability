using OpenTraceability.Models.MasterData;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.MasterData
{
    public class GDSTLocation : Location
    {
        [OpenTraceabilityArray]
        [OpenTraceabilityObject]
        [OpenTraceabilityJson("gdst:locationClassification")]
        [OpenTraceabilityMasterData("urn:gdst:kde#locationClassification")]
        public List<GDSTClassification> LocationClassification { get; set; } = new List<GDSTClassification>();

        [OpenTraceabilityJson("geoLocation")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#geoLocation")]
        public string? GeoLocation { get; set; }

        [OpenTraceabilityJson("geoFence")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#geoFence")]
        public string? GeoFence { get; set; }

        [OpenTraceabilityJson("cbvmda:vesselFlagState")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#vesselFlagState")]
        public Country? VesselFlagState { get; set; }

        [OpenTraceabilityJson("cbvmda:vesselID")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#vesselID")]
        public string? VesselID { get; set; }

        [OpenTraceabilityJson("cbvmda:vesselName")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#vesselName")]
        public string? VesselName { get; set; }

        [OpenTraceabilityJson("gdst:imoNumber")]
        [OpenTraceabilityMasterData("urn:gdst:kde#imoNumber")]
        public string? IMONumber { get; set; }

        [OpenTraceabilityJson("gdst:vesselPublicRegistry")]
        [OpenTraceabilityMasterData("urn:gdst:kde#vesselPublicRegistry")]
        public string? VesselPublicRegistry { get; set; }

        [OpenTraceabilityJson("gdst:satelliteTracking")]
        [OpenTraceabilityMasterData("urn:gdst:kde#satelliteTracking")]
        public string? SatelliteTrackingAuthority { get; set; }
    }
}
