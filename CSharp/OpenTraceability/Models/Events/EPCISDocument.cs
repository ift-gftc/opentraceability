namespace OpenTraceability.Models.Events
{
    public class EPCISDocument : EPCISBaseDocument
    {
        public EPCISQueryDocument ToEPCISQueryDocument()
        {
            EPCISQueryDocument document = new EPCISQueryDocument();
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