using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events.KDEs
{
    public class EventKDEString : EventKDEBase, IEventKDE
    {
        public Type ValueType => typeof(string);
        public string? Value { get; set; }

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
                XName xsiTypeName = (XNamespace)Constants.XSI_NAMESPACE + "type";
                x.Add(new XAttribute(xsiTypeName, "string"));

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