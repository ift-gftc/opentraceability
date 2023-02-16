using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Interfaces
{
    public enum VocabularyType
    {
        Unknown = 0,

        [Description("urn:epcglobal:epcis:vtype:EPCClass")]
        Tradeitem = 1,

        [Description("urn:epcglobal:epcis:vtype:Location")]
        Location = 2,

        [Description("urn:epcglobal:epcis:vtype:Party")]
        TradingParty = 3
    }

    public interface IVocabularyElement
    {
        /// <summary>
        /// The ID of the master data object.
        /// </summary>
        public string? ID { get; }

        /// <summary>
        /// The type of the vocabulary.
        /// </summary>
        public string? Type { get; set; }

        /// <summary>
        /// The enum representing the vocabulary type.
        /// </summary>
        public VocabularyType VocabularyType { get; }

        /// <summary>
        /// These are additional KDEs that were not mapped into the object.
        /// </summary>
        List<IMasterDataKDE> KDEs { get; set; }
    }
}
