using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;

namespace OpenTraceability.Models.MasterData
{
    public class VocabularyElement : IVocabularyElement
    {
        public string ID { get; set; }

        public string EPCISType { get; set; }

        [OpenTraceabilityJson("@type")]
        public string JsonLDType { get; set; }

        public JToken Context { get; set; }

        public VocabularyType VocabularyType
        {
            get
            {
                VocabularyType type = VocabularyType.Unknown;
                foreach (VocabularyType t in Enum.GetValues(typeof(VocabularyType)))
                {
                    if (EnumUtil.GetEnumDescription(t).Trim().ToLower() == EPCISType?.Trim().ToLower())
                    {
                        type = t;
                    }
                }
                // if we can't determine the type from the EPCIS type, then try to determine it from the JSON-LD type
                if (type == VocabularyType.Unknown)
                {
                    if (!string.IsNullOrEmpty(JsonLDType))
                    {
                        switch(JsonLDType.Trim().ToLower())
                        {
                            case "gs1:seafood":
                            case "gs1:product":
                                type = VocabularyType.Tradeitem;
                                break;
                            case "gs1:organization":
                                type = VocabularyType.TradingParty;
                                break;
                            case "gs1:place":
                                type = VocabularyType.Location;
                                break;
                        }
                    }
                }
                return type;
            }
        }

        public List<IMasterDataKDE> KDEs { get; set; } = new List<IMasterDataKDE>();
    }
}
