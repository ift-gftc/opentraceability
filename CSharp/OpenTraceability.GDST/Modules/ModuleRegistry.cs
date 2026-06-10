using OpenTraceability.GDST;

namespace OpenTraceability.GDST.Modules
{
    /// <summary>
    /// Maps GDST JSON-LD property keys (KDEs) to the non-core module that "owns" them. This is the
    /// self-contained module map shared by the SDK and consumers: only non-core keys are listed here,
    /// so any key NOT present is treated as Core and always served. Keys are matched against the
    /// serialized JSON-LD property names emitted by OpenTraceability.GDST (e.g. <c>gdst:broodstockSource</c>).
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
            ["cbvmda:informationProvider"] = GdstModule.Seafood,
            ["cbvmda:productionMethodForFishAndSeafoodCode"] = GdstModule.Seafood,
            ["readPoint"] = GdstModule.Seafood,
            ["cbvmda:owning_party"] = GdstModule.Seafood,
            ["cbvmda:tradeItemConditionCode"] = GdstModule.Seafood,
            ["cbvmda:certificationList"] = GdstModule.Seafood,
            ["cbvmda:countryOfOrigin"] = GdstModule.Seafood,
        };

        /// <summary>
        /// Maps source/destination list entry <c>type</c> values (CBV source-destination terms) to the
        /// module that owns them. The party terms are Seafood-level chain-of-custody information;
        /// <c>location</c> is Core and is always kept (returns null).
        /// </summary>
        private static readonly Dictionary<string, GdstModule> _sourceDestTypeToModule = new(System.StringComparer.OrdinalIgnoreCase)
        {
            ["owning_party"] = GdstModule.Seafood,
            ["possessing_party"] = GdstModule.Seafood,
        };

        /// <summary>
        /// Local-name (prefix-stripped, lowercased) lookup derived from <see cref="_keyToModule"/>.
        /// Lets callers that work with C# property names / KDE names (rather than prefixed JSON-LD
        /// keys) share the same module map. Built lazily on first use.
        /// </summary>
        private static Dictionary<string, GdstModule>? _localNameToModule;

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

        /// <summary>
        /// Returns the module that owns the given source/destination list entry <c>type</c> value
        /// (e.g. <c>owning_party</c>), or null if it is Core (e.g. <c>location</c>). Tolerates both the
        /// bare CBV term and full URI forms (e.g. <c>urn:epcglobal:cbv:sdt:owning_party</c>) by matching
        /// on the trailing term.
        /// </summary>
        public static GdstModule? GetModuleForSourceDestinationType(string? type)
        {
            string term = LocalName(type);
            if (_sourceDestTypeToModule.TryGetValue(term, out var module))
            {
                return module;
            }
            return null;
        }

        /// <summary>
        /// Returns the module that owns the given local name (a C# property name or KDE name, matched
        /// case-insensitively against the prefix-stripped JSON-LD keys), or null if it is Core. Used by
        /// callers that compare in-memory models rather than serialized JSON-LD.
        /// </summary>
        public static GdstModule? GetModuleForLocalName(string? name)
        {
            if (string.IsNullOrWhiteSpace(name)) return null;

            var map = _localNameToModule;
            if (map == null)
            {
                map = new Dictionary<string, GdstModule>(System.StringComparer.OrdinalIgnoreCase);
                foreach (var kvp in _keyToModule)
                {
                    map[LocalName(kvp.Key)] = kvp.Value;
                }
                _localNameToModule = map;
            }

            if (map.TryGetValue(name!.Trim(), out var module))
            {
                return module;
            }
            return null;
        }

        /// <summary>
        /// Strips any namespace prefix / URI path from an identifier, returning the trailing term
        /// lowercased. Handles <c>gdst:broodstockSource</c>, <c>urn:epcglobal:cbv:sdt:owning_party</c>,
        /// <c>https://ref.gs1.org/cbv/SDT-location</c>, and bare names.
        /// </summary>
        private static string LocalName(string? identifier)
        {
            if (string.IsNullOrWhiteSpace(identifier)) return string.Empty;
            string s = identifier!.Trim();

            int slash = s.LastIndexOf('/');
            if (slash >= 0 && slash < s.Length - 1) s = s.Substring(slash + 1);

            int colon = s.LastIndexOf(':');
            if (colon >= 0 && colon < s.Length - 1) s = s.Substring(colon + 1);

            // Some CBV web vocabulary terms use a "SDT-owning_party" style suffix.
            int dash = s.LastIndexOf('-');
            if (dash >= 0 && dash < s.Length - 1 && s.StartsWith("SDT-", System.StringComparison.OrdinalIgnoreCase))
            {
                s = s.Substring(dash + 1);
            }

            return s.ToLowerInvariant();
        }
    }
}
