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
        /// The version of EPCIS used for this EPCIS Document / EPCIS Query Document.
        /// </summary>
        public EPCISVersion? EPCISVersion { get; set; }

        /// <summary>
        /// The date that the EPCIS Document / EPCIS Query Document was created.
        /// </summary>
        public DateTimeOffset? CreationDate { get; set; }

        /// <summary>
        /// The standard business document header on the EPCIS Document / EPCIS Query Document.
        /// </summary>
        public StandardBusinessDocumentHeader? Header { get; set; }

        /// <summary>
        /// One or more events on the EPCIS Document / EPCIS Query Document.
        /// </summary>
        public List<IEvent> Events { get; set; } = new List<IEvent>();

        /// <summary>
        /// All of the master data vocab elements included in the EPCIS Document / EPCIS Query Document.
        /// </summary>
        public List<IVocabularyElement> MasterData { get; set; } = new List<IVocabularyElement>();

        /// <summary>
        /// These are namespaces that are included in the EPCIS Document / EPCIS Query Document. For JSON-LD, where the @context information
        /// is just namespaces, they will be placed here.
        /// </summary>
        public Dictionary<string, string> Namespaces { get; set; } = new Dictionary<string, string>();

        /// <summary>
        /// These are JSON-LD contexts that are included in the EPCIS Document / EPCIS Query Document. These can either be a URL to the 
        /// context file, or the context as a JOBject converted into string.
        /// </summary>
        public List<string> Contexts { get; set; } = new List<string>();

        /// <summary>
        /// These are attributes attached to the EPCIS Document / EPCIS Query Document including all attributes except the creation date.
        /// </summary>
        public Dictionary<string, string> Attributes { get; set; } = new Dictionary<string, string>();

        /// <summary>
        /// Returns master data elements of a specific type.
        /// </summary>
        public List<T> GetMasterData<T>() where T : IVocabularyElement
        {
            return this.MasterData.Where(m => m is T).Select(m => (T)m).ToList();
        }

        /// <summary>
        /// Returns master data elements of a specific type.
        /// </summary>
        public T? GetMasterData<T>(string id) where T : IVocabularyElement
        {
            return this.GetMasterData<T>().FirstOrDefault(m => m is T && m.ID?.ToLower() == id.ToLower());
        }

        /// <summary>
        /// Merges the epcis document into the this one.
        /// </summary>
        /// <param name="data"></param>
        public void Merge(EPCISBaseDocument data)
        {
            this.Events.AddRange(data.Events.Where(e => !this.Events.Exists(e2 => e.EventID == e2.EventID)));
            this.MasterData.AddRange(data.MasterData.Where(p => !this.MasterData.Exists(p2 => p.ID == p2.ID)));
        }
    }
}