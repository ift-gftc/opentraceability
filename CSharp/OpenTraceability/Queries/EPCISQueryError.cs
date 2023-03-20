using System;
using System.Net;

namespace OpenTraceability.Queries
{
	/// <summary>
	/// Represents the type of error that occured while querying of an EPCIS Query Interface.
	/// </summary>
	public enum EPCISQueryErrorType
	{
		HTTP,
		Schema,
        Exception
    }

	/// <summary>
	/// Represents an error that occured during the querying of an EPCIS Query Interface.
	/// </summary>
	public class EPCISQueryError
	{
		/// <summary>
		/// The type of error that occured.
		/// </summary>
		public EPCISQueryErrorType Type { get; set; }

		/// <summary>
		/// Details about the error that occured.
		/// </summary>
		public string Details { get; set; } = string.Empty;

		/// <summary>
		/// The ID of the stack trace item that is connected to this error. If stack trace is disabled,
		/// then this will be an empty string.
		/// </summary>
		public string StackTraceItemID { get; set; } = string.Empty;
	}
}

