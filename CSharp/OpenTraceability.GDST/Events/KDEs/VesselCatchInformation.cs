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
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "catchArea", 1)]
        public string? CatchArea { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "economicZone", 0)]
        public string? EconomicZone { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "fishingGearTypeCode", 0)]
        public string? GearType { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "vesselFlagState", 0)]
        public Country? VesselFlagState { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "vesselID", 0)]
        public string? VesselID { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "vesselName", 0)]
        public string? VesselName { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "fisheryImprovementProject", 0)]
        public string? FIP { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "gpsAvailability", 0)]
        public bool GPSAvailability { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "imoNumber", 0)]
        public string? IMONumber { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "rfmoArea", 0)]
        public string? RFMO { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "satelliteTrackingAuthority", 0)]
        public string? SatelliteTrackingAuthority { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "subnationalPermitArea", 0)]
        public string? SubNationalPermitArea { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "vesselPublicRegistry", 0)]
        public string? VesselPublicRegistry { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "vesselTripDate", 0)]
        public DateTimeOffset? VesselTripDate { get; set; }

        [OpenTraceabilityExtensionElements]
        public List<IEventKDE> KDEs { get; set; } = new List<IEventKDE>();
    }
}