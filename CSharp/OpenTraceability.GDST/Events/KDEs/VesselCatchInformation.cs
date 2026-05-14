using OpenTraceability.Interfaces;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events.KDEs
{
    public class VesselCatchInformation
    {
        [OpenTraceabilityJson("cbvmda:catchArea")]
        public string? CatchArea { get; set; }

        [OpenTraceabilityJson("cbvmda:economicZone")]
        public string? EconomicZone { get; set; }

        [OpenTraceabilityJson("cbvmda:fishingGearTypeCode")]
        public string? GearType { get; set; }

        [OpenTraceabilityJson("cbvmda:vesselFlagState")]
        public Country? VesselFlagState { get; set; }

        [OpenTraceabilityJson("cbvmda:vesselID")]
        public string? VesselID { get; set; }

        [OpenTraceabilityJson("cbvmda:vesselName")]
        public string? VesselName { get; set; }

        [OpenTraceabilityJson("gdst:fisheryImprovementProject")]
        public string? FIP { get; set; }

        [OpenTraceabilityJson("gdst:gpsAvailability")]
        public bool GPSAvailability { get; set; }

        [OpenTraceabilityJson("gdst:imoNumber")]
        public string? IMONumber { get; set; }

        [OpenTraceabilityJson("gdst:rfmoArea")]
        public string? RFMO { get; set; }

        [OpenTraceabilityJson("gdst:satelliteTrackingAuthority")]
        public string? SatelliteTrackingAuthority { get; set; }

        [OpenTraceabilityJson("gdst:subnationalPermitArea")]
        public string? SubNationalPermitArea { get; set; }

        [OpenTraceabilityJson("gdst:vesselPublicRegistry")]
        public string? VesselPublicRegistry { get; set; }

        [OpenTraceabilityJson("gdst:vesselTripDate")]
        public DateTimeOffset? VesselTripDate { get; set; }

        [OpenTraceabilityExtensionElements]
        public List<IEventKDE> KDEs { get; set; } = new List<IEventKDE>();
    }
}
