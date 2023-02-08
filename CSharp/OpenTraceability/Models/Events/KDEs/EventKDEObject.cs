using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events.KDEs
{
    public class EventKDEObject : EventKDEBase, IEventKDE
    {
        private XElement? _xml = null;
        private JToken? _json = null;

        public Type ValueType => typeof(object);
        public object? Value
        {
            get => (_xml != null) ? _xml : _json;
        }

        internal EventKDEObject()
        {

        }

        public EventKDEObject(string ns, string name)
        {
            this.Namespace = ns;
            this.Name = name;
        }

        public void SetFromJson(JToken json)
        {
            _xml = null;
            _json = json;
        }

        public JToken? GetJson()
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

        public void SetFromXml(XElement xml)
        {
            _xml = xml;
            _json = null;
        }

        public XElement? GetXml()
        {
            if (_xml != null)
            {
                return _xml;
            }
            else if (_json != null)
            {
                // convert _json to XElement
                string? xmlStr = (JsonConvert.DeserializeXmlNode(_json.ToString()) as XmlDocument)?.OuterXml;
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
