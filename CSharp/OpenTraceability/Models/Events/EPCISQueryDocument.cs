using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    public class EPCISQueryDocument : EPCISBaseDocument
    {
        public string QueryName { get; set; } = string.Empty;

        public string SubscriptionID { get; set; } = string.Empty;

        public EPCISDocument ToEPCISDocument()
        {
            EPCISDocument document = new EPCISDocument();
            var props = typeof(EPCISBaseDocument).GetProperties();
            foreach (var p in props)
            {
                var v = p.GetValue(this);
                p.SetValue(document, v);
            }
            return document;
        }
    }
}