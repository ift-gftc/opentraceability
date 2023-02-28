using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Utility.Attributes
{
    /// <summary>
    /// This attribute indicates how the property is mapped into the EPCIS Master Data header attributes.
    /// </summary>
    public class OpenTraceabilityMasterDataAttribute : Attribute
    {
        public string Name { get; set; }

        public OpenTraceabilityMasterDataAttribute()
        {
            Name = string.Empty;
        }

        public OpenTraceabilityMasterDataAttribute(string name)
        {
            Name = name;
        }

        public OpenTraceabilityMasterDataAttribute(string ns, string name)
        {
            Name = (((XNamespace)ns) + name).ToString();
        }
    }
}
