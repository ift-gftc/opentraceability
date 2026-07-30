using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Queries
{
    /// <summary>
    /// Options for using the MasterDataResolver class.
    /// </summary>
    public class DigitalLinkQueryOptions
    {
        /// <summary>
        /// The URL of the EPCIS Query Interface.
        /// </summary>
        public Uri? URL { get; set; }

        /// <summary>
        /// The value of the X-API-Key header to use when querying the EPCIS Query Interface.
        /// Only required if a preconfigured HTTP client is NOT being used with the resolver consuming this object.
        /// </summary>
        public string? APIKey { get; set; }

        /// <summary>
        /// Additional HTTP headers to add to each request made by the resolver (e.g. X-Dataset-Id).
        /// A header is only added if the request does not already contain it.
        /// </summary>
        public Dictionary<string, string> Headers { get; set; } = new Dictionary<string, string>();

        /// <summary>
        /// The version of EPCIS that we are querying for. This defaults to EPCIS 2.0
        /// </summary>
        public EPCISVersion Version { get; set; } = EPCISVersion.V2;

        /// <summary>
        /// The version of the GS1 Digital Link / Resolver standard the resolver conforms to.
        /// This defaults to <see cref="ResolverVersion.ResolverStandard_1_2_0"/> (the RFC 9264
        /// linkset behavior); set it to <see cref="ResolverVersion.ResolverStandard_1_1_2"/> to
        /// resolve against a legacy resolver that returns a flat digital link array.
        /// </summary>
        public ResolverVersion ResolverVersion { get; set; } = ResolverVersion.ResolverStandard_1_2_0;

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
