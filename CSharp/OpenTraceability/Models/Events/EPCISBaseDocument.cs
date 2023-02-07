using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Identifiers;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events
{
    public enum EPCISVersion
    {
        Version_1_2 = 0,
        Version_2_0 = 1
    }

    public class EPCISBaseDocument
    {
        /// <summary>
        /// Represents a list of namespaces used in the document.
        /// </summary>
        public Dictionary<string, string> Namespaces { get; set; } = new Dictionary<string, string>();

        /// <summary>
        /// The version of EPCIS used for this document.
        /// </summary>
        public EPCISVersion? EPCISVersion { get; set; }

        /// <summary>
        /// The date that the EPCIS Document was created.
        /// </summary>
        public DateTime? CreationDate { get; set; }

        /// <summary>
        /// The standard business document header on the EPCIS document.
        /// </summary>
        public StandardBusinessDocumentHeader? Header { get; set; }

        /// <summary>
        /// One or more events on the EPCIS Document.
        /// </summary>
        public List<IEvent> Events { get; set; } = new List<IEvent>();
    }
}