using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Utility.Attributes
{
    /// <summary>
    /// This attribute is meant to override the OpenTraceabilityAttribute.Name for mapping into and out of JSON-LD / JSON.
    /// </summary>
    public class OpenTraceabilityJsonAttribute : System.Attribute
    {
        public string Name { get; set; }

        public OpenTraceabilityJsonAttribute(string name)
        {
            Name = name;
        }
    }
}
