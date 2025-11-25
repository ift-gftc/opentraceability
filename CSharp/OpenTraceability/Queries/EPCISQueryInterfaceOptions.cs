using System;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;

namespace OpenTraceability.Queries
{
   

	/// <summary>
	/// Configuration used for talking to the EPCIS Query Interface
	/// containing things like URL and other options.
	/// </summary>
	public class EPCISQueryInterfaceOptions
	{
        /// <summary>
        /// The URL of the EPCIS Query Interface.
        /// </summary>
        public Uri URL { get; set; }

        /// <summary>
        /// The value of the X-API-Key header to use when querying the EPCIS Query Interface.
        /// Only required if a preconfigured HTTP client is NOT being used with the resolver consuming this object.
        /// </summary>
        public string? APIKey { get; set; }

        /// <summary>
        /// The version of EPCIS that we are querying for. This defaults to EPCIS 2.0
        /// </summary>
        public EPCISVersion Version { get; set; } = EPCISVersion.V2;

        /// <summary>
        /// The data format to expect the data back in.
        /// </summary>
        public EPCISDataFormat Format { get; set; } = EPCISDataFormat.JSON;

        /// <summary>
        /// Determines if it will build the stack trace while querying. This is
        /// set to TRUE by default.
        /// </summary>
        public bool EnableStackTrace { get; set; } = true;
    }
}

