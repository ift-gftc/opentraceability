using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Models.MasterData
{
    public class Tradeitem : IVocabularyElement
    {
        public string ID { get => GTIN?.ToString(); }

        public string EPCISType { get; set; } = "urn:epcglobal:epcis:vtype:EPCClass";

        [OpenTraceabilityJson("@type")]
        public string JsonLDType { get; set; } = "gs1:Product";

        public VocabularyType VocabularyType => VocabularyType.Tradeitem;

        public JToken Context { get; set; }

        [OpenTraceabilityJson("gtin")]
        public GTIN GTIN { get; set; }

        [OpenTraceabilityJson("productName")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#descriptionShort")]
        public List<LanguageString> ShortDescription { get; set; }

        [OpenTraceabilityJson("cbvmda:tradeItemConditionCode")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#tradeItemConditionCode")]
        public string TradeItemConditionCode { get; set; }

        [OpenTraceabilityJson("cbvmda:owning_party")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:owning_party")]
        public PGLN OwningParty { get; set; }

        [OpenTraceabilityJson("cbvmda:informationProvider")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#informationProvider")]
        public PGLN InformationProvider { get; set; }

        [OpenTraceabilityArray]
        [OpenTraceabilityJson("cbvmda:speciesForFisheryStatisticsPurposesName")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesName")]
        public List<string> FisherySpeciesScientificName { get; set; }

        [OpenTraceabilityArray]
        [OpenTraceabilityJson("cbvmda:speciesForFisheryStatisticsPurposesCode")]
        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesCode")]
        public List<string> FisherySpeciesCode { get; set; }

        public List<IMasterDataKDE> KDEs { get; set; } = new List<IMasterDataKDE>();
    }
}
