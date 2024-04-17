using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Utility.Attributes
{
    public class OpenTraceabilityArrayAttribute : System.Attribute
    {
        public string ItemName { get; set; }

        public OpenTraceabilityArrayAttribute() { }

        public OpenTraceabilityArrayAttribute(string listXName)
        {
            ItemName = listXName;
        }
    }
}
