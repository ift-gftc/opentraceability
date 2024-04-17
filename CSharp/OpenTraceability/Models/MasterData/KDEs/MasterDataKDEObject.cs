using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.MasterData.KDEs;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events.KDEs
{
    public class MasterDataKDEObject : MasterDataKDEBase, IMasterDataKDE
    {
        private XElement _xml = null;
        private JToken _json = null;

        public Type ValueType => typeof(object);
        public object Value
        {
            get
            {
                if (_xml != null)
                {
                    return _xml;
                }
                else
                {
                    return _json;
                }
            }
        }

        internal MasterDataKDEObject()
        {

        }

        public MasterDataKDEObject(string ns, string name)
        {
            this.Namespace = ns;
            this.Name = name;
        }

        public void SetFromGS1WebVocabJson(JToken json)
        {
            _xml = null;
            _json = json;
        }

        public JToken GetGS1WebVocabJson()
        {
            if (_xml != null)
            {
                // convert _xml to JObject
                var j = Newtonsoft.Json.JsonConvert.SerializeXNode(_xml);
                return j;
            }
            else if (_json != null)
            {
                return _json;
            }
            else
            {
                return null;
            }
        }

        public void SetFromEPCISXml(XElement xml)
        {
            _xml = xml;
            _json = null;
        }

        public XElement GetEPCISXml()
        {
            if (_xml != null)
            {
                return _xml;
            }
            else if (_json != null)
            {
                // convert _json to XElement
                string xmlStr = (JsonConvert.DeserializeXmlNode(_json.ToString()) as XmlDocument)?.OuterXml;
                if (!string.IsNullOrEmpty(xmlStr))
                {
                    return XElement.Parse(xmlStr);
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }
}
