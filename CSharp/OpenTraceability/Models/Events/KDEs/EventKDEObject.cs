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

        public JToken GetJson()
        {
            if (_xml != null)
            {
                // convert _xml to JObject
                using (XmlReader xmlReader = _xml.CreateReader())
                {
                    XmlDocument xmlDoc = new XmlDocument();
                    xmlDoc.Load(xmlReader);

                    var j = Newtonsoft.Json.JsonConvert.SerializeXmlNode(xmlDoc, Newtonsoft.Json.Formatting.Indented, true);
                    return JToken.Parse(j);
                }
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

        public XElement GetXml()
        {
            if (_xml != null)
            {
                return _xml;
            }
            else if (_json != null)
            {
                string xmlStr = string.Empty;

                JArray jArray = _json as JArray;
                JObject j = _json as JObject;
                if (j != null && j.Properties().Count() > 1)
                {
                    xmlStr = (JsonConvert.DeserializeXmlNode(_json.ToString(), Namespace + Name) as XmlDocument)?.OuterXml;
                    XElement x = new XElement(XElement.Parse(xmlStr));
                    return x;
                }
                else if (jArray != null)
                {
                    XElement xList = new XElement(Namespace + Name);
                    foreach (JObject jObject in jArray)
                    {
                        string itemXML = (JsonConvert.DeserializeXmlNode(jObject.ToString(), "Item") as XmlDocument)?.OuterXml;
                        XElement xItem = new XElement(XElement.Parse(itemXML));
                        xList.Add(xItem);
                    }
                    return xList;
                }
                else
                {
                    xmlStr = (JsonConvert.DeserializeXmlNode(_json.ToString()) as XmlDocument)?.OuterXml;
                }

                if (!string.IsNullOrEmpty(xmlStr))
                {
                    XElement x = new XElement((XNamespace)Namespace + Name, XElement.Parse(xmlStr));
                    return x;
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
