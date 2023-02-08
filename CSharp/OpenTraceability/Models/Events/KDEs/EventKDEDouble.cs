using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events.KDEs
{
    public class EventKDEDouble : EventKDEBase, IEventKDE
    {
        public Type ValueType => typeof(double);
        public double? Value { get; set; }

        internal EventKDEDouble()
        {

        }

        public EventKDEDouble(string ns, string name)
        {
            this.Namespace = ns;
            this.Name = name;
        }

        public JToken? GetJson()
        {
            if (Value == null)
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
            if (Value == null)
            {
                return null;
            }
            else
            {
                XName xname = (XNamespace)Namespace + Name;
                XElement x = new XElement(xname, Value);

                // set the xsi type...
                XName xsiTypeName = (XNamespace)Constants.XSI_NAMESPACE + "type";
                x.Add(new XAttribute(xsiTypeName, "number"));

                return x;
            }
        }

        public void SetFromJson(JToken json)
        {
            this.Value = double.Parse(json.ToString());
        }

        public void SetFromXml(XElement xml)
        {
            this.Value = double.Parse(xml.Value);
        }

        public override string ToString()
        {
            return Value?.ToString() ?? string.Empty;
        }
    }
}