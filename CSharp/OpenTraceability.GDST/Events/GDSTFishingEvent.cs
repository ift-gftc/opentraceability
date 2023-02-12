using OpenTraceability.GDST.Events.KDEs;
using OpenTraceability.Models.Events;

namespace OpenTraceability.GDST.Events
{
    /// <summary>
    /// Represents a fishing event that follows the GDST 1.2 seafood traceability standard.
    /// </summary>
    public class GDSTFishingEvent : ObjectEvent
    {
        public GDSTFishingEvent()
        {

        }

        public VesselCatchInformationList? VesselCatchInformationList { get; set; }

        public string CatchArea { get; set; }
        
        public string EEZ { get; set; }

        public string RFMO { get; set; }

        public string SubNationalPermitArea { get; set; }

        public string FishingGear { get; set; }

        public string FIP { get; set; }
    }
}