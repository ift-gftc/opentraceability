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
        /// Maps certification <c>gdst:certificationType</c> values (full URNs) to the module that owns
        /// them. Same "null = keep" contract as <see cref="_sourceDestTypeToModule"/>: a type NOT
        /// present here is unlabelled and is always kept by the minifier.
        /// </summary>
        private static readonly Dictionary<string, GdstModule> _certificationTypeToModule = new(System.StringComparer.OrdinalIgnoreCase)
        {
            ["urn:gdst:certType:harvestCoC"] = GdstModule.Seafood,
            ["urn:gdst:certType:harvestCert"] = GdstModule.Seafood,
            ["urn:gdst:certType:humanPolicy"] = GdstModule.Seafood,
            ["urn:gdst:certType:processorLicense"] = GdstModule.Seafood,
            ["urn:gdst:certType:legalAuth"] = GdstModule.Aquaculture,
            ["urn:gdst:certType:landingAuth"] = GdstModule.Wildcaught,
            ["urn:gdst:certType:transshipmentAuth"] = GdstModule.Wildcaught,
            ["urn:gdst:certType:fishingAuth"] = GdstModule.Wildcaught,
        };

        /// <summary>
        /// Maps product classification values (the <c>value</c> of a <c>gdst:productClassification</c>
        /// entry) to the module that owns them. Unlike <see cref="_keyToModule"/>, this map is a strict
        /// allow-list: a value NOT present here is unknown and is stripped by the minifier for every
        /// module set. Keys are stored pre-normalized (letters/digits only, lowercase) to match
        /// however the value is cased in the wild (e.g. "WildCaught").
        /// </summary>
        private static readonly Dictionary<string, GdstModule> _productClassificationToModule = new()
        {
            ["seafood"] = GdstModule.Seafood,
            ["processed"] = GdstModule.Seafood,
            ["wildcaught"] = GdstModule.Wildcaught,
            ["farmed"] = GdstModule.Aquaculture,
            ["feed"] = GdstModule.Aquaculture,
            ["developing"] = GdstModule.Aquaculture,
            ["mature"] = GdstModule.Aquaculture,
            ["live"] = GdstModule.Aquaculture,
        };

        /// <summary>
        /// Maps location classification values (the <c>value</c> of a <c>gdst:locationClassification</c>
        /// entry) to the module that owns them. Same strict allow-list convention as
        /// <see cref="_productClassificationToModule"/>: "land facility" is explicitly Core so it is
        /// served to every tier, and unknown values are stripped.
        /// </summary>
        private static readonly Dictionary<string, GdstModule> _locationClassificationToModule = new()
        {
            //["land facility"] = GdstModule.Core,
            ["processor"] = GdstModule.Seafood,
            ["vessel"] = GdstModule.Wildcaught,
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
        /// Returns the module that owns the given certification <c>gdst:certificationType</c> value
        /// (e.g. <c>urn:gdst:certType:legalAuth</c>), or null when the type is unknown or blank.
        /// Unknown types are considered unlabelled and are kept by the minifier, matching the
        /// contract of <see cref="GetModuleForSourceDestinationType"/> rather than the strict
        /// allow-list used for classifications.
        /// </summary>
        public static GdstModule? GetModuleForCertificationType(string? certificationType)
        {
            if (string.IsNullOrWhiteSpace(certificationType))
            {
                return null;
            }

            if (_certificationTypeToModule.TryGetValue(certificationType!.Trim(), out var module))
            {
                return module;
            }
            return null;
        }

        /// <summary>
        /// Returns the module that owns the given product classification value (e.g. <c>wildCaught</c>),
        /// or null when the value is unknown. Matching ignores case and non-alphanumeric characters.
        /// Note the null contract differs from <see cref="GetModule"/>: an unknown classification value
        /// is NOT Core - it is unrecognized and should be stripped by the minifier.
        /// </summary>
        public static GdstModule? GetModuleForProductClassification(string? value)
        {
            if (_productClassificationToModule.TryGetValue(NormalizeClassification(value), out var module))
            {
                return module;
            }
            return null;
        }

        /// <summary>
        /// Returns the module that owns the given location classification value (e.g. <c>land facility</c>),
        /// or null when the value is unknown. Matching ignores case and non-alphanumeric characters.
        /// Note the null contract differs from <see cref="GetModule"/>: an unknown classification value
        /// is NOT Core - it is unrecognized and should be stripped by the minifier.
        /// </summary>
        public static GdstModule? GetModuleForLocationClassification(string? value)
        {
            if (_locationClassificationToModule.TryGetValue(NormalizeClassification(value), out var module))
            {
                return module;
            }
            return null;
        }

        /// <summary>
        /// Normalizes a classification value to letters/digits only, lowercased, so "Land Facility",
        /// "landFacility", and "land facility" all compare equal. Mirrors GDSTEventProfileResolver's
        /// normalization so classification matching behaves the same everywhere.
        /// </summary>
        private static string NormalizeClassification(string? value)
        {
            if (string.IsNullOrWhiteSpace(value))
            {
                return string.Empty;
            }

            return new string(value!.Where(char.IsLetterOrDigit).ToArray()).ToLowerInvariant();
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
