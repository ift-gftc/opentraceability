using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events.KDEs
{
    public class EventKDEString : EventKDEBase, IEventKDE
    {
        public Type ValueType => typeof(string);
        public string? Value { get; set; }
        public string? Type { get; set; }
        Dictionary<string, string> Attributes { get; set; } = new Dictionary<string, string>();

        internal EventKDEString()
        {

        }

        public EventKDEString(string ns, string name)
        {
            this.Namespace = ns;
            this.Name = name;
        }

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
                XName xname = (XNamespace)Namespace + Name;
                XElement x = new XElement(xname, Value);

                // set the xsi type...
                foreach (var kvp in Attributes)
                {
                    x.Add(new XAttribute(kvp.Key, kvp.Value));
                }

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

            foreach (XAttribute xatt in xml.Attributes())
            {
                Attributes.Add(xatt.Name.ToString(), xatt.Value);
            }
        }

        public override string ToString()
        {
            return Value ?? string.Empty;
        }
    }
}