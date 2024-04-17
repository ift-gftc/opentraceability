using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Queries;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Linq;
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
        public StandardBusinessDocumentHeader Header { get; set; }

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
        public T GetMasterData<T>(string id) where T : IVocabularyElement
        {
            return this.GetMasterData<T>().FirstOrDefault(m => m is T && m.ID?.ToLower() == id?.ToLower());
        }

        /// <summary>
        /// Merges the EPCIS document into the this one. If multiple copies of an event exist in the case of an error declaration,
        /// we will keep the error declared event.
        /// </summary>
        /// <param name="data"></param>
        public void Merge(EPCISBaseDocument data)
        {
            foreach (var e in data.Events)
            {
                bool found = false;
                foreach (var e2 in this.Events.ToList())
                {
                    if (e.EventID == e2.EventID)
                    {
                        if (e.ErrorDeclaration == null && e2.ErrorDeclaration != null)
                        {
                            this.Events.Remove(e);
                            this.Events.Add(e2);
                        }
                        found = true;
                    }
                }
                if (!found)
                {
                    this.Events.Add(e);
                }
            }
            this.MasterData.AddRange(data.MasterData.Where(p => !this.MasterData.Exists(p2 => p.ID == p2.ID)));
        }

        /// <summary>
        /// Applies one or more EPCIS Query Parameters to the events and returns the ones that match the parameters.
        /// </summary>
        public List<IEvent> FilterEvents(EPCISQueryParameters parameters)
        {
            List<IEvent> events = new List<IEvent>();

            foreach (var evt in this.Events)
            {
                // filter: GE_eventTime
                if (parameters.query.GE_eventTime != null)
                {
                    if (evt.EventTime == null || evt.EventTime < parameters.query.GE_eventTime)
                    {
                        continue;
                    }
                }

                // filter: LE_eventTime
                if (parameters.query.LE_eventTime != null)
                {
                    if (evt.EventTime == null || evt.EventTime > parameters.query.LE_eventTime)
                    {
                        continue;
                    }
                }

                // filter: GE_recordTime
                if (parameters.query.GE_recordTime != null)
                {
                    if (evt.RecordTime == null || evt.RecordTime < parameters.query.GE_recordTime)
                    {
                        continue;
                    }
                }

                // filter: LE_recordTime
                if (parameters.query.LE_recordTime != null)
                {
                    if (evt.RecordTime == null || evt.RecordTime > parameters.query.LE_recordTime)
                    {
                        continue;
                    }
                }

                // filter: EQ_bizStep
                if (parameters.query.EQ_bizStep != null && parameters.query.EQ_bizStep.Count > 0)
                {
                    if (!HasUriMatch(evt.BusinessStep, parameters.query.EQ_bizStep, "https://ref.gs1.org/cbv/BizStep-", "urn:epcglobal:cbv:bizstep:"))
                    {
                        continue;
                    }
                }

                // filter: EQ_bizLocation
                if (parameters.query.EQ_bizLocation != null && parameters.query.EQ_bizLocation.Count > 0)
                {
                    if (evt.Location?.GLN == null || !parameters.query.EQ_bizLocation.Select(e => e.ToString().ToLower()).Contains(evt.Location.GLN.ToString().ToLower()))
                    {
                        continue;
                    }
                }

                // filter: MATCH_anyEPC
                if (parameters.query.MATCH_anyEPC != null && parameters.query.MATCH_anyEPC.Count > 0)
                {
                    if (!HasMatch(evt, parameters.query.MATCH_anyEPC))
                    {
                        continue;
                    }
                }

                // filter: MATCH_anyEPCClass
                if (parameters.query.MATCH_anyEPCClass != null && parameters.query.MATCH_anyEPCClass.Count > 0)
                {
                    if (!HasMatch(evt, parameters.query.MATCH_anyEPCClass))
                    {
                        continue;
                    }
                }

                // filter: MATCH_epc
                if (parameters.query.MATCH_epc != null && parameters.query.MATCH_epc.Count > 0)
                {
                    if (!HasMatch(evt, parameters.query.MATCH_epc, EventProductType.Reference, EventProductType.Child))
                    {
                        continue;
                    }
                }

                // filter: MATCH_epcClass
                if (parameters.query.MATCH_epcClass != null && parameters.query.MATCH_epcClass.Count > 0)
                {
                    if (!HasMatch(evt, parameters.query.MATCH_epcClass, EventProductType.Reference, EventProductType.Child))
                    {
                        continue;
                    }
                }

                events.Add(evt);
            }

            return events;
        }

        private bool HasMatch(IEvent evt, List<string> epcs, params EventProductType[] allowedTypes)
        {
            foreach (var epc_matchStr in epcs)
            {
                EPC epc_match = new EPC(epc_matchStr);
                foreach (var product in evt.Products)
                {
                    if (allowedTypes.Count() == 0 || allowedTypes.Contains(product.Type))
                    {
                        if (epc_match.Matches(product.EPC))
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private bool HasUriMatch(Uri uri, List<string> filter, string prefix, string replacePrefix)
        {
            // make sure all of the EQ_bizStep are converted into URI format before comparing
            for (int i = 0; i < filter.Count; i++)
            {
                string bizStep = filter[i];
                if (!Uri.TryCreate(bizStep, UriKind.Absolute, out Uri u))
                {
                    filter[i] = replacePrefix + bizStep;
                }
                else if (bizStep.StartsWith(prefix))
                {
                    filter[i] = replacePrefix + bizStep.Split('-').Last();
                }
            }

            // we need to handle the various formats that the bizStep can occur in
            if (uri != null)
            {
                Uri bizStep = new Uri(uri.ToString());
                if (bizStep.ToString().StartsWith(prefix))
                {
                    bizStep = new Uri(replacePrefix + uri.ToString().Split('-').Last());
                }

                List<Uri> filter_uris = filter.Select(x => new Uri(x)).ToList();
                if (!filter_uris.Select(u => u.ToString().ToLower()).Contains(bizStep.ToString().ToLower()))
                {
                    return false;
                }
            }
            else
            {
                return false;
            }

            return true;
        }
    }
}