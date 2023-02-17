using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Utility.Attributes
{
    public class OpenTraceabilityCURIEMapping : System.Attribute
    {
        public string URIPrefix { get; set; }

        public OpenTraceabilityCURIEMapping(string uRIPrefix)
        {
            URIPrefix = uRIPrefix;
        }
    }
}
