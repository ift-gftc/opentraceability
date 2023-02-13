using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Identifiers;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events
{
    public enum EPCISVersion
    {
        Version_1_2 = 1,
        Version_2_0 = 2
    }

    public class EPCISBaseDocument
    {
        /// <summary>
        /// Represents a list of namespaces used in the document.
        /// </summary>
        internal Dictionary<string, string> Namespaces { get; set; } = new Dictionary<string, string>();

        /// <summary>
        /// The version of EPCIS used for this document.
        /// </summary>
        public EPCISVersion? EPCISVersion { get; set; }

        /// <summary>
        /// The date that the EPCIS Document was created.
        /// </summary>
        public DateTimeOffset? CreationDate { get; set; }

        /// <summary>
        /// The standard business document header on the EPCIS document.
        /// </summary>
        public StandardBusinessDocumentHeader? Header { get; set; }

        /// <summary>
        /// One or more events on the EPCIS Document.
        /// </summary>
        public List<IEvent> Events { get; set; } = new List<IEvent>();

        public Dictionary<string, string> GetNamespaces()
        {
            Dictionary<string, string> namespaces = new Dictionary<string, string>(Namespaces);
            return namespaces;
        }
    }
}