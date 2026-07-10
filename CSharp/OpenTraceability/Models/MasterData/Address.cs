using OpenTraceability.Models.Common;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Models.MasterData
{
    public class Address
    {
        [OpenTraceabilityJson("@type")]
        public string Type { get; set; } = "gs1:PostalAddress";

        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#streetAddressOne")]
        [OpenTraceabilityJson("streetAddress")]
        public List<LanguageString>? Address1 { get; set; }

        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#streetAddressTwo")]
        public List<LanguageString>? Address2 { get; set; }

        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#city")]
        [OpenTraceabilityJson("addressLocality")]
        public List<LanguageString>? City { get; set; }

        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#state")]
        [OpenTraceabilityJson("addressRegion")]
        public List<LanguageString>? State { get; set; }

        [OpenTraceabilityJson("postalCode")]
        public string PostalCode { get; set; } = string.Empty;

        [OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#countryCode")]
        [OpenTraceabilityJson("countryCode")]
        public Country? Country { get; set; }

        /// <summary>
        /// The country of the address in the GS1 Web Vocabulary format, as a nested gs1:Country object.
        /// Only used on the GS1 Web Vocab JSON path; the EPCIS master data path uses <see cref="Country"/> instead.
        /// </summary>
        [OpenTraceabilityObject]
        [OpenTraceabilityJson("addressCountry")]
        public AddressCountry? AddressCountry { get; set; }
    }
}
