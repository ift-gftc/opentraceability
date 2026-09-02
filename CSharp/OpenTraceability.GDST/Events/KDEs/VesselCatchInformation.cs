using OpenTraceability.Interfaces;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events.KDEs
{
    public class VesselCatchInformation
    {
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "catchArea")]
        [OpenTraceabilityJson("cbvmda:catchArea")]
        public string? CatchArea { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "economicZone")]
        [OpenTraceabilityJson("cbvmda:economicZone")]
        public string? EconomicZone { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "fishingGearTypeCode")]
        [OpenTraceabilityJson("cbvmda:fishingGearTypeCode")]
        public string? GearType { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "vesselFlagState")]
        [OpenTraceabilityJson("cbvmda:vesselFlagState")]
        public Country? VesselFlagState { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "vesselID")]
        [OpenTraceabilityJson("cbvmda:vesselID")]
        public string? VesselID { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "vesselName")]
        [OpenTraceabilityJson("cbvmda:vesselName")]
        public string? VesselName { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "fisheryImprovementProject")]
        [OpenTraceabilityJson("gdst:fisheryImprovementProject")]
        public string? FIP { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "gpsAvailability")]
        [OpenTraceabilityJson("gdst:gpsAvailability")]
        public bool GPSAvailability { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "imoNumber")]
        [OpenTraceabilityJson("gdst:imoNumber")]
        public string? IMONumber { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "rfmoArea")]
        [OpenTraceabilityJson("gdst:rfmoArea")]
        public string? RFMO { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "satelliteTrackingAuthority")]
        [OpenTraceabilityJson("gdst:satelliteTrackingAuthority")]
        public string? SatelliteTrackingAuthority { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "subnationalPermitArea")]
        [OpenTraceabilityJson("gdst:subnationalPermitArea")]
        public string? SubNationalPermitArea { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "vesselPublicRegistry")]
        [OpenTraceabilityJson("gdst:vesselPublicRegistry")]
        public string? VesselPublicRegistry { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "vesselTripDate")]
        [OpenTraceabilityJson("gdst:vesselTripDate")]
        public DateTimeOffset? VesselTripDate { get; set; }

        [OpenTraceabilityExtensionElements]
        public List<IEventKDE> KDEs { get; set; } = new List<IEventKDE>();
    }
}
