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

        /// <summary>
        /// Applies an optional X-API-Key and any additional custom headers to the request. Each header is
        /// only added if the request does not already contain it, and adding never throws.
        /// </summary>
        public static void ApplyOptionHeaders(this HttpRequestHeaders headers, string? apiKey, IDictionary<string, string>? customHeaders)
        {
            if (!string.IsNullOrEmpty(apiKey) && !headers.Contains("X-API-Key"))
            {
                headers.TryAddWithoutValidation("X-API-Key", apiKey);
            }

            if (customHeaders != null)
            {
                foreach (var header in customHeaders)
                {
                    if (!string.IsNullOrEmpty(header.Key) && !headers.Contains(header.Key))
                    {
                        headers.TryAddWithoutValidation(header.Key, header.Value);
                    }
                }
            }
        }
    }
}
