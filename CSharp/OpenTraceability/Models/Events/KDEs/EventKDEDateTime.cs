using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events.KDEs
{
    public class EventKDEDateTime : EventKDEBase, IEventKDE
    {
        public Type ValueType => typeof(DateTimeOffset?);
        public DateTimeOffset? Value { get; set; }

        public JToken? GetJson()
        {
            throw new NotImplementedException();
        }

        public XElement? GetXml()
        {
            throw new NotImplementedException();
        }

        public void SetFromJson(JToken json)
        {
            throw new NotImplementedException();
        }

        public void SetFromXml(XElement xml)
        {
            throw new NotImplementedException();
        }

        public override string ToString()
        {
            return Value?.ToString("O") ?? String.Empty;
        }
    }
}