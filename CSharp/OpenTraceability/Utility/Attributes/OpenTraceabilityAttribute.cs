using OpenTraceability.Models.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Utility.Attributes
{
    [AttributeUsage(AttributeTargets.Property, AllowMultiple = true)]
    public class OpenTraceabilityAttribute : System.Attribute
    { 
        public string Name { get; set; }

        public int? SequenceOrder { get; set; }

        public EPCISVersion? Version { get; set; }

        public OpenTraceabilityAttribute(string name)
        {
            Name = name;
        }

        public OpenTraceabilityAttribute(string ns, string name)
        {
            Name = (((XNamespace)ns) + name).ToString();
        }

        public OpenTraceabilityAttribute(string name, int sequenceOrder)
        {
            Name = name;
            SequenceOrder = sequenceOrder;
        }

        public OpenTraceabilityAttribute(string ns, string name, int sequenceOrder)
        {
            Name = (((XNamespace)ns) + name).ToString();
            SequenceOrder = sequenceOrder;
        }

        public OpenTraceabilityAttribute(string name, EPCISVersion version)
        {
            Name = name;
            Version = version;
        }

        public OpenTraceabilityAttribute(string ns, string name, EPCISVersion version)
        {
            Name = (((XNamespace)ns) + name).ToString();
            Version = version;
        }

        public OpenTraceabilityAttribute(string name, int sequenceOrder, EPCISVersion version)
        {
            Name = name;
            SequenceOrder = sequenceOrder;
            Version = version;
        }

        public OpenTraceabilityAttribute(string ns, string name, int sequenceOrder, EPCISVersion version)
        {
            Name = (((XNamespace)ns) + name).ToString();
            SequenceOrder = sequenceOrder;
            Version = version;
        }
    }
}
