using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Text;

namespace OpenTraceability.Models.MasterData
{
    /// <summary>
    /// The country of a gs1:PostalAddress in the GS1 Web Vocabulary format, carried in the "addressCountry"
    /// property as a nested gs1:Country object (https://ref.gs1.org/voc/Country).
    /// </summary>
    public class AddressCountry
    {
        /// <summary>
        /// The JSON-LD @type of the value. Always "gs1:Country".
        /// </summary>
        [OpenTraceabilityJson("@type")]
        public string Type { get; set; } = "gs1:Country";

        /// <summary>
        /// The ISO 3166-1 alpha-2 country code, e.g. "GB".
        /// </summary>
        [OpenTraceabilityJson("countryCode")]
        public Country? CountryCode { get; set; }
    }
}
