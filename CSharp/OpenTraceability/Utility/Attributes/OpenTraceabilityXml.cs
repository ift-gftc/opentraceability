using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Utility.Attributes
{
    public class OpenTraceabilityXmlAttribute : System.Attribute
    {
        public XName Name { get; set; }

        public OpenTraceabilityXmlAttribute(string name)
        {
            Name = name;
        }
    }
}
