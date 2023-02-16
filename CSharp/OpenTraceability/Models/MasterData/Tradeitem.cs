using OpenTraceability.Interfaces;
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
        public string? ID { get => GTIN?.ToString(); }
        public string? Type { get; set; } = "urn:epcglobal:epcis:vtype:EPCClass";
        public VocabularyType VocabularyType => VocabularyType.Tradeitem;

        public GTIN? GTIN { get; set; }

        [OpenTraceability("urn:epcglobal:cbv:mda#descriptionShort", 1)]
        public string? ShortDescription { get; set; }

        [OpenTraceability("urn:epcglobal:cbv:mda#tradeItemConditionCode", 2)]
        public string? TradeItemConditionCode { get; set; }

        [OpenTraceability("urn:epcglobal:cbv:owning_Party", 3)]
        public PGLN? OwningParty { get; set; }

        [OpenTraceability("urn:epcglobal:cbv:mda#informationProvider", 4)]
        public PGLN? InformationProvider { get; set; }

        [OpenTraceability("urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesName", 5)]
        public string? FisherySpeciesScientificName { get; set; }

        [OpenTraceability("urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesCode", 6)]
        public string? FisherySpeciesCode { get; set; }

        [OpenTraceability("https://gs1.org/cbv/cbvmda:certificationList", Events.EPCISVersion.V1)]
        public CertificationList? CertificationList { get; set; }

        public List<IMasterDataKDE> KDEs { get; set; } = new List<IMasterDataKDE>();
    }
}
