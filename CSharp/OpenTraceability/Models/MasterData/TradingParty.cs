using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Models.MasterData
{
    public class TradingParty : IVocabularyElement
    {
        public string? ID { get => PGLN?.ToString(); }

        public string? EPCISType { get; set; } = "urn:epcglobal:epcis:vtype:Party";

        [OpenTraceabilityJson("@type")]
        public string? JsonLDType { get; set; } = "gs1:Organization";

        public VocabularyType VocabularyType => VocabularyType.TradingParty;

        public JToken? Context { get; set; }

        [OpenTraceabilityJson("globalLocationNumber")]
        public PGLN? PGLN { get; set; }

        [OpenTraceabilityJson("cbvmda:owning_party")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:owning_party")]
        public PGLN? OwningParty { get; set; }

        [OpenTraceabilityJson("cbvmda:informationProvider")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#informationProvider")]
        public PGLN? InformationProvider { get; set; }

        [OpenTraceabilityJson("organizationName")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#name")]
        public List<LanguageString>? Name { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceabilityJson("address")]
        public Address? Address { get; set; }

        [OpenTraceabilityJson("gdst:iftp")]
        [OpenTraceabilityMasterData("urn:gdst:kde#iftp")]
        public string? IFTP { get; set; }

        /// <summary>
        /// These are additional KDEs that were not mapped into the object.
        /// </summary>
        public List<IMasterDataKDE> KDEs { get; set; } = new List<IMasterDataKDE>();
    }
}
