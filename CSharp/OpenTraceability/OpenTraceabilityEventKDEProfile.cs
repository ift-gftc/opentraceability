using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability
{
    /// <summary>
    /// Used for profiling events based on the presence of one or more KDEs.
    /// </summary>
    public class OpenTraceabilityEventKDEProfile
    {
        public OpenTraceabilityEventKDEProfile(string xPath_V1, string xPath_V2, string jPath)
        {
            XPath_V1 = xPath_V1;
            XPath_V2 = xPath_V2;
            JPath = jPath;
        }

        public string XPath_V1 { get; set; }
        public string XPath_V2 { get; set; }
        public string JPath { get; set; }

        public override string ToString()
        {
            return string.Format("{0}:{1}:{2}", XPath_V1, XPath_V2, JPath);
        }
    }
}
