using OpenTraceability.Models.MasterData;
using OpenTraceability.Utility.Attributes;
using OpenTraceability.Utility;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.GDST.MasterData
{
    public class GDSTLocation : Location
    {
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
