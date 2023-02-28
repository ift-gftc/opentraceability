using OpenTraceability.Interfaces;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System.Xml.Linq;

namespace OpenTraceability.GDST.Events.KDEs
{
    /// <summary>
    /// Represents vessel catch information found in the ILMD of a fishing event.
    /// </summary>
    public class VesselCatchInformation
    {
        [OpenTraceabilityJson("cbvmda:catchArea")]
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "catchArea", 1)]
        public string? CatchArea { get; set; }

        [OpenTraceabilityJson("cbvmda:economicZone")]
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "economicZone", 0)]
        public string? EconomicZone { get; set; }

        [OpenTraceabilityJson("cbvmda:fishingGearTypeCode")]
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "fishingGearTypeCode", 0)]
        public string? GearType { get; set; }

        [OpenTraceabilityJson("cbvmda:vesselFlagState")]
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "vesselFlagState", 0)]
        public Country? VesselFlagState { get; set; }

        [OpenTraceabilityJson("cbvmda:vesselID")]
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "vesselID", 0)]
        public string? VesselID { get; set; }

        [OpenTraceabilityJson("cbvmda:vesselName")]
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "vesselName", 0)]
        public string? VesselName { get; set; }

        [OpenTraceabilityJson("gdst:fisheryImprovementProject")]
        [OpenTraceability(Constants.GDST_NAMESPACE, "fisheryImprovementProject", 0)]
        public string? FIP { get; set; }

        [OpenTraceabilityJson("gdst:gpsAvailability")]
        [OpenTraceability(Constants.GDST_NAMESPACE, "gpsAvailability", 0)]
        public bool GPSAvailability { get; set; }

        [OpenTraceabilityJson("gdst:imoNumber")]
        [OpenTraceability(Constants.GDST_NAMESPACE, "imoNumber", 0)]
        public string? IMONumber { get; set; }

        [OpenTraceabilityJson("gdst:rfmoArea")]
        [OpenTraceability(Constants.GDST_NAMESPACE, "rfmoArea", 0)]
        public string? RFMO { get; set; }

        [OpenTraceabilityJson("gdst:satelliteTrackingAuthority")]
        [OpenTraceability(Constants.GDST_NAMESPACE, "satelliteTrackingAuthority", 0)]
        public string? SatelliteTrackingAuthority { get; set; }

        [OpenTraceabilityJson("gdst:subnationalPermitArea")]
        [OpenTraceability(Constants.GDST_NAMESPACE, "subnationalPermitArea", 0)]
        public string? SubNationalPermitArea { get; set; }

        [OpenTraceabilityJson("gdst:vesselPublicRegistry")]
        [OpenTraceability(Constants.GDST_NAMESPACE, "vesselPublicRegistry", 0)]
        public string? VesselPublicRegistry { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "vesselTripDate", 0)]
        public DateTimeOffset? VesselTripDate { get; set; }

        [OpenTraceabilityExtensionElements]
        public List<IEventKDE> KDEs { get; set; } = new List<IEventKDE>();
    }
}