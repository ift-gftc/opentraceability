using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Utility.Attributes
{
    /// <summary>
    /// Tells the XML serializer to ignore this property. It is for JSON-LD only.
    /// </summary>
    internal class OpenTraceabilityXmlIgnoreAttribute : System.Attribute
    {
    }
}
