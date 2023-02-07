using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Utility;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events.KDEs
{
    public class EventKDECountry : EventKDEBase, IEventKDE
    {
        public Country? Value { get; set; }

        public Type ValueType => typeof(Country);

        public JToken? GetJson()
        {
            if (Value == null)
            {
                return null;
            }
            else
            {
                return JToken.FromObject(Value.ISO);
            }
        }

        public XElement? GetXml()
        {
            if (Value == null)
            {
                return null;
            }
            else
            {
                return new XElement(Key, Value.ISO);
            }
        }

        public void SetFromJson(JToken json)
        {
            string strValue = json.ToString();
            Value = Countries.Parse(strValue); 
        }

        public void SetFromXml(XElement xml)
        {
            Value = Countries.Parse(xml.Value);
        }

        public override string ToString()
        {
            return Value?.Name ?? String.Empty;
        }
    }
}