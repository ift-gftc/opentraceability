using System;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;

namespace OpenTraceability.Queries
{
    public enum EPCISQueryType
    {
        Unknown = 0,
        events = 1
    }

    public class EPCISQueryParameters
    {
        public EPCISQueryParameters()
        {

        }

        /// <summary>
        /// Constructs the parameters from a list of one or more epcs and populates
        /// the MATCH_anyEPC and MATCH_anyEPCClass parameters.
        /// </summary>
        /// <param name="epcs"></param>
        public EPCISQueryParameters(params EPC[] epcs)
        {
            foreach (var epc in epcs)
            {
                if (epc.Type == EPCType.Class)
                {
                    if (query.MATCH_anyEPCClass == null)
                    {
                        query.MATCH_anyEPCClass = new List<string>();
                    }
                    query.MATCH_anyEPCClass.Add(epc.ToString());
                }
                else
                {
                    if (query.MATCH_anyEPC == null)
                    {
                        query.MATCH_anyEPC = new List<string>();
                    }
                    query.MATCH_anyEPC.Add(epc.ToString());
                }
            }
        }

        public EPCISQueryType queryType { get; set; } = EPCISQueryType.events;

        public EPCISQuery query { get; set; } = new EPCISQuery();

        public bool IsValid(out string? error)
        {
            error = null;
            return true;
        }

        public string ToJson()
        {
            string json = JsonConvert.SerializeObject(this.query, new JsonSerializerSettings()
            {
                Formatting = Formatting.Indented,
                DateFormatString = "o",
                NullValueHandling = NullValueHandling.Ignore
            });

            JObject jobject = new JObject();
            jobject["queryType"] = this.queryType.ToString();
            jobject["query"] = JObject.Parse(json);

            return jobject.ToString();
        }
    }

    public class EPCISQuery
    {
        public DateTime? GE_recordTime { get; set; }
        public DateTime? LE_recordTime { get; set; }
        public DateTime? GE_eventTime { get; set; }
        public DateTime? LE_eventTime { get; set; }
        public List<string>? eventTypes { get; set; } = new List<string>();
        public List<string>? MATCH_epc { get; set; }
        public List<string>? MATCH_epcClass { get; set; }
        public List<string>? MATCH_anyEPC { get; set; }
        public List<string>? MATCH_anyEPCClass { get; set; }
        public List<Uri>? EQ_bizStep { get; set; }
        public List<Uri>? EQ_bizLocation { get; set; }
    }
}

