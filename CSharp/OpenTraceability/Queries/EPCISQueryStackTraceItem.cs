using System;
using System.Net;

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
		public Uri? RelativeURL { get; set; }

		/// <summary>
		/// The HTTP headers of the request.
		/// </summary>
		public List<KeyValuePair<string, IEnumerable<string>>>? RequestHeaders { get; set; }

		/// <summary>
		/// The HTTP headers of the response.
		/// </summary>
		public List<KeyValuePair<string, IEnumerable<string>>>? ResponseHeaders { get; set; }

		/// <summary>
		/// The raw contents of the body of the HTTP request.
		/// </summary>
		public string? RequestBody { get; set; }

		/// <summary>
		/// The raw contents of the body of the HTTP response.
		/// </summary>
		public string? ResponseBody { get; set; }
	}
}

