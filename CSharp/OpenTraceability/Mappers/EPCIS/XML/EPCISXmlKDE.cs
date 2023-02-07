using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Mappers.EPCIS.XML
{
    /// <summary>
    /// This represents a KDE to be processed in an EPCIS event.
    /// </summary>
    public class EPCISXmlKDE
    {
        public string Name { get; set; } = string.Empty;
        public EPCISVersion? RequiredVersion { get; set; }
        public Action<IEvent, XElement> Action { get; set; }

        public EPCISXmlKDE(string name, Action<IEvent, XElement> action, EPCISVersion? requiredVersion = null)
        {
            Name = name;
            Action = action;
            RequiredVersion = requiredVersion;
        }
    }
}
