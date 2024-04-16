using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Models.MasterData
{
    public class VocabularyElement : IVocabularyElement
    {
        public string ID { get; set; }

        public string EPCISType { get; set; }

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
                return type;
            }
        }

        public List<IMasterDataKDE> KDEs { get; set; } = new List<IMasterDataKDE>();
    }
}
