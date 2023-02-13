using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Mappers.EPCIS
{
    /// <summary>
    /// This represents a KDE to mapping configuration to be used by the EPCIS mappers.
    /// </summary>
    public class EPCISMappingKDE
    {
        /** STATIC **/

        private static object _locker = new object();
        private static Dictionary<string, List<EPCISMappingKDE>>? _mappingKDEs = null;
        public static Dictionary<string, List<EPCISMappingKDE>> MappingKDEs
        {
            get
            {
                if (_mappingKDEs == null)
                {
                    lock (_locker)
                    {
                        _mappingKDEs = new Dictionary<string, List<EPCISMappingKDE>>();
                        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
                        string jsonStr = loader.ReadString("OpenTraceability", "OpenTraceability.Mappers.EPCIS.mappings.json");
                        JObject json = JObject.Parse(jsonStr);
                        JObject? jEPCIS = json["EPCIS"] as JObject;
                        if (jEPCIS != null)
                        {
                            foreach (JProperty jprop in jEPCIS.Properties())
                            {
                                JArray? jarr = jEPCIS[jprop.Name] as JArray;
                                if (jarr != null)
                                {
                                    List<EPCISMappingKDE>? kdes = JsonConvert.DeserializeObject<List<EPCISMappingKDE>>(jarr.ToString());
                                    if (kdes != null)
                                    {
                                        _mappingKDEs.Add(jprop.Name, kdes);
                                    }
                                }
                            }
                        }
                    }
                }
                return _mappingKDEs;
            }
        }

        /** PROPERTIES **/
        public string XPath { get; set; } = string.Empty;
        public string? Property { get; set; }
        public string? Type { get; set; }
        public EPCISVersion? Version { get; set; }
    }
}
