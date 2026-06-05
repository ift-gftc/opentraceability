using System.Collections.Generic;

namespace OpenTraceability.TestServer.Core.Modules
{
    /// <summary>
    /// Maps GDST JSON-LD property keys (KDEs) to the non-core module that "owns" them. This is the
    /// self-contained module map for the test server: only non-core keys are listed here, so any
    /// key NOT present is treated as Core and always served. Keys are matched against the serialized
    /// JSON-LD property names emitted by OpenTraceability.GDST (e.g. <c>gdst:broodstockSource</c>).
    ///
    /// Removing a container key (e.g. <c>cbvmda:vesselCatchInformationList</c>) prunes its whole
    /// subtree, so listing nested keys is belt-and-suspenders rather than strictly required.
    /// Sources: OpenTraceability.GDST GDSTILMD / GDSTLocation / GDSTTradeItem / GDST*Event /
    /// VesselCatchInformation, cross-checked against GDSTTools ModuleFactory and the example JSON-LD.
    /// </summary>
    public static class ModuleRegistry
    {
        private static readonly Dictionary<string, GdstModule> _keyToModule = new(System.StringComparer.OrdinalIgnoreCase)
        {
            // ---- Aquaculture (farmed production) ----
            ["gdst:broodstockSource"] = GdstModule.Aquaculture,
            ["gdst:aquacultureMethod"] = GdstModule.Aquaculture,
            ["gdst:proteinSource"] = GdstModule.Aquaculture,

            // ---- Wildcaught (vessel / fishing) ----
            ["cbvmda:vesselCatchInformationList"] = GdstModule.Wildcaught,
            ["cbvmda:vesselCatchInformation"] = GdstModule.Wildcaught,
            ["cbvmda:catchArea"] = GdstModule.Wildcaught,
            ["cbvmda:economicZone"] = GdstModule.Wildcaught,
            ["cbvmda:fishingGearTypeCode"] = GdstModule.Wildcaught,
            ["cbvmda:vesselFlagState"] = GdstModule.Wildcaught,
            ["cbvmda:vesselID"] = GdstModule.Wildcaught,
            ["cbvmda:vesselName"] = GdstModule.Wildcaught,
            ["gdst:fisheryImprovementProject"] = GdstModule.Wildcaught,
            ["gdst:gpsAvailability"] = GdstModule.Wildcaught,
            ["gdst:imoNumber"] = GdstModule.Wildcaught,
            ["gdst:rfmoArea"] = GdstModule.Wildcaught,
            ["gdst:satelliteTrackingAuthority"] = GdstModule.Wildcaught,
            ["gdst:satelliteTracking"] = GdstModule.Wildcaught,
            ["gdst:subnationalPermitArea"] = GdstModule.Wildcaught,
            ["gdst:vesselPublicRegistry"] = GdstModule.Wildcaught,
            ["gdst:vesselTripDate"] = GdstModule.Wildcaught,

            // ---- Seafood (general GDST seafood KDEs shared by wild + aqua) ----
            ["cbvmda:unloadingPort"] = GdstModule.Seafood,
            ["cbvmda:speciesForFisheryStatisticsPurposesName"] = GdstModule.Seafood,
            ["cbvmda:speciesForFisheryStatisticsPurposesCode"] = GdstModule.Seafood,
            ["gdst:locationClassification"] = GdstModule.Seafood,
            ["gdst:productClassification"] = GdstModule.Seafood,
            ["gdst:humanWelfarePolicy"] = GdstModule.Seafood,
            ["gdst:productOwner"] = GdstModule.Seafood,
            ["gdst:iftp"] = GdstModule.Seafood,
            ["geoLocation"] = GdstModule.Seafood,
            ["geoFence"] = GdstModule.Seafood,
        };

        /// <summary>
        /// Returns the module that owns the given JSON-LD property key, or null if the key is
        /// considered Core (always served).
        /// </summary>
        public static GdstModule? GetModule(string key)
        {
            if (_keyToModule.TryGetValue(key, out var module))
            {
                return module;
            }
            return null;
        }
    }
}
