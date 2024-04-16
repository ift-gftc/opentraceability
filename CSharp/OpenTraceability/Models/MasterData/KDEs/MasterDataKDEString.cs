using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.MasterData.KDEs;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events.KDEs
{
    public class MasterDataKDEString : MasterDataKDEBase, IMasterDataKDE
    {
        public Type ValueType => typeof(string);
        public string Value { get; set; }
        public string Type { get; set; }
        Dictionary<string, string> Attributes { get; set; } = new Dictionary<string, string>();

        internal MasterDataKDEString()
        {

        }

        public MasterDataKDEString(string ns, string name)
        {
            this.Namespace = ns;
            this.Name = name;
        }

        public JToken GetJson()
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

        public XElement GetXml()
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

        public void SetFromGS1WebVocabJson(JToken json)
        {
            this.Value = json.ToString();
        }

        public JToken GetGS1WebVocabJson()
        {
            if (this.Value != null)
            {
                return JToken.FromObject(this.Value);
            }
            else
            {
                return null;
            }
        }

        public void SetFromEPCISXml(XElement xml)
        {
            Name = xml.Attribute("id")?.Value ?? string.Empty;
            Value = xml.Value;
        }

        public XElement GetEPCISXml()
        {
            if (Value != null)
            {
                XElement x = new XElement("attribute");
                x.Add(new XAttribute("id", Name));
                x.Value = Value;
                return x;
            }
            else
            {
                return null;
            }
        }
    }
}