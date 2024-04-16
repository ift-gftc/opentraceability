using System;
using System.Collections.Generic;
using System.Net;
using System.Text;

namespace OpenTraceability.Queries
{
	/// <summary>
	/// Represents a single HTTP request in the EPCIS Query Stack Trace.
	/// </summary>
	public class EPCISQueryStackTraceItem
	{
		/// <summary>
		/// A unique GUID generated for the stack trace item to give it an ID.
		/// </summary>
		public string ID { get; set; } = Guid.NewGuid().ToString();

		/// <summary>
		/// The UTC datetime that the request was executed.
		/// </summary>
		public DateTime Created { get; set; } = DateTime.UtcNow;

		/// <summary>
		/// The status code of the response that was returned.
		/// </summary>
		public HttpStatusCode? ResponseStatusCode { get; set; }

		/// <summary>
		/// The relative URL that was executed against the base URL of the EPCIS Query Interface.
		/// </summary>
		public Uri RelativeURL { get; set; }

		/// <summary>
		/// The HTTP headers of the request.
		/// </summary>
		public List<KeyValuePair<string, IEnumerable<string>>> RequestHeaders { get; set; }

		/// <summary>
		/// The HTTP headers of the response.
		/// </summary>
		public List<KeyValuePair<string, IEnumerable<string>>> ResponseHeaders { get; set; }

		/// <summary>
		/// The raw contents of the body of the HTTP request.
		/// </summary>
		public string RequestBody { get; set; }

		/// <summary>
		/// The raw contents of the body of the HTTP response.
		/// </summary>
		public string ResponseBody { get; set; }

        public override string ToString()
        {
            StringBuilder stringBuilder = new StringBuilder();

            // write out all the properties to the string builder
            stringBuilder.AppendLine("HTTP REQUEST::");
            stringBuilder.AppendLine("ID: " + ID);
            stringBuilder.AppendLine("Created: " + Created);
            stringBuilder.AppendLine("ResponseStatusCode: " + ResponseStatusCode);
            stringBuilder.AppendLine("RelativeURL: " + RelativeURL);

            if (RequestHeaders != null)
            {
                stringBuilder.AppendLine("RequestHeaders:");
                foreach (var kvp in RequestHeaders)
                {
                    stringBuilder.AppendLine("\t" + kvp.Key + ": " + string.Join(", ", kvp.Value));
                }
            }

            if (ResponseHeaders != null)
            {
                stringBuilder.AppendLine("ResponseHeaders:");
                foreach (var kvp in ResponseHeaders)
                {
                    stringBuilder.AppendLine("\t" + kvp.Key + ": " + string.Join(", ", kvp.Value));
                }
            }

            stringBuilder.AppendLine("RequestBody:\n" + RequestBody);
            stringBuilder.AppendLine("ResponseBody:\n" + ResponseBody);

            return stringBuilder.ToString();
        }
    }
}

