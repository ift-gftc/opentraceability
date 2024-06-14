using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http.Headers;
using System.Text;

namespace OpenTraceability.Utility
{
    public static class HeaderExtensions
    {
        /// <summary>
        /// Ensures that the GS1-Extensions header is present in the headers collection.
        /// </summary>
        /// <param name="headers"></param>
        public static void AddGDSTExtensionHeader(this HttpRequestHeaders headers)
        {
            if (!headers.Contains("GS1-Extensions"))
            {
                headers.Add("GS1-Extensions", "gdst=https://traceability-dialogue.org/epcis");
            }
            else
            {
                List<string> extensions = headers.GetValues("GS1-Extensions").ToList();
                if (!extensions.Contains("gdst=https://traceability-dialogue.org/epcis"))
                {
                    extensions.Add("gdst=https://traceability-dialogue.org/epcis");
                    headers.Remove("GS1-Extensions");
                    headers.Add("GS1-Extensions", extensions);
                }
            }
        }
    }
}
