using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.MasterData.KDEs;
using OpenTraceability.Utility;
using System;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events.KDEs
{
    public class MasterDataKDECountry : MasterDataKDEBase, IMasterDataKDE
    {
        public Country Value { get; set; }

        public Type ValueType => typeof(Country);

        public XElement GetEPCISXml()
        {
            if (Value != null)
            {
                XElement x = new XElement("attribute");
                x.Add(new XAttribute("id", Name));
                x.Value = Value.Alpha3;
                return x;
            }
            else
            {
                return null;
            }
        }

        public JToken GetGS1WebVocabJson()
        {
            throw new NotImplementedException();
        }

        public XElement GetXml()
        {
            if (Value == null)
            {
                return null;
            }
            else
            {
                XName xname = (XNamespace)Namespace + Name;
                XElement x = new XElement(xname, Value.ISO);
                return x;
            }
        }

        public void SetFromEPCISXml(XElement xml)
        {
            Country c = Countries.Parse(xml.Value);
            this.Value = c;
            this.Name = xml.Attribute("id")?.Value ?? string.Empty;
        }

        public void SetFromGS1WebVocabJson(JToken json)
        {
            throw new NotImplementedException();
        }

        public override string ToString()
        {
            return Value?.Name ?? String.Empty;
        }
    }
}