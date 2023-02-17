using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Utility.Attributes;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events
{
    public enum EPCISVersion
    {
        V1 = 1,
        V2 = 2
    }

    public class EPCISBaseDocument
    {
        /// <summary>
        /// The version of EPCIS used for this document.
        /// </summary>
        public EPCISVersion? EPCISVersion { get; internal set; }

        /// <summary>
        /// The date that the EPCIS Document was created.
        /// </summary>
        public DateTimeOffset? CreationDate { get; set; }

        /// <summary>
        /// The standard business document header on the EPCIS document.
        /// </summary>
        public StandardBusinessDocumentHeader? Header { get; set; }

        /// <summary>
        /// One or more events on the EPCIS Query Document.
        /// </summary>
        public List<IEvent> Events { get; set; } = new List<IEvent>();

        /// <summary>
        /// All of the master data vocab elements included in the EPCIS Query Document.
        /// </summary>
        public List<IVocabularyElement> MasterData { get; set; } = new List<IVocabularyElement>();

        /// <summary>
        /// These are attributes attached to the EPCIS document including all attributes except the creation date. Including things like namespaces.
        /// </summary>
        public Dictionary<string, string> Attributes { get; set; } = new Dictionary<string, string>();
    }
}