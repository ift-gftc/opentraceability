using System;
using OpenTraceability.Models.Events;

namespace OpenTraceability.Queries
{
	public class EPCISQueryResults
	{
		/// <summary>
		/// A summary of all of the EPCIS events.
		/// </summary>
		public EPCISQueryDocument? Document { get; set; }

		/// <summary>
		/// A summary of all HTTP requests that were performed while resolving
		/// traceability data.
		/// </summary>
		public List<EPCISQueryStackTraceItem> StackTrace { get; set; } = new List<EPCISQueryStackTraceItem>();

		/// <summary>
		/// A list of errors that occured during the querying of traceability data from
		/// an EPCIS Query Interface.
		/// </summary>
		public List<EPCISQueryError> Errors { get; set; } = new List<EPCISQueryError>();

		public EPCISQueryResults()
		{

		}

		public void Merge(EPCISQueryResults results)
		{
			this.StackTrace.AddRange(results.StackTrace);
			this.Errors.AddRange(results.Errors);

			if (this.Document == null)
			{
				this.Document = results.Document;
			}
			else if (results.Document != null)
			{
                this.Document.Merge(results.Document);
            }
		}
	}
}

