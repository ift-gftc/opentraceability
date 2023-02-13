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
        public string Name { get; set; }

        public int SequenceOrder { get; set; } = 0;

        public OpenTraceabilityXmlAttribute(string name)
        {
            Name = name;
        }

        public OpenTraceabilityXmlAttribute(string name, int sequenceOrder)
        {
            Name = name;
            SequenceOrder = sequenceOrder;
        }

        public OpenTraceabilityXmlAttribute(string ns, string name, int sequenceOrder)
        {
            Name = (((XNamespace)ns) + name).ToString();
            SequenceOrder = sequenceOrder;
        }
    }
}
