using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events.KDEs
{
    public class EventKDEString : EventKDEBase, IEventKDE
    {
        public Type ValueType => typeof(string);
        public string? Value { get; set; }

        public JToken? GetJson()
        {
            if (string.IsNullOrWhiteSpace(Value))
            {
                return null;
            }
            else
            {
                return JToken.FromObject(Value);
            }
        }

        public XElement? GetXml()
        {
            if (string.IsNullOrWhiteSpace(Value))
            {
                return null;
            }
            else
            {
                XElement x = new XElement(Key, Value);
                return x;
            }
        }

        public void SetFromJson(JToken json)
        {
            this.Value = json.ToString();
        }

        public void SetFromXml(XElement xml)
        {
            this.Value = xml.Value;
        }

        public override string ToString()
        {
            return Value ?? string.Empty;
        }
    }
}