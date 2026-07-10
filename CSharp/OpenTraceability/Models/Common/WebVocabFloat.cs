using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Text;

namespace OpenTraceability.Models.Common
{
    /// <summary>
    /// A GS1 Web Vocabulary typed float value in its expanded JSON-LD form, e.g. { "@value": "51.5175264", "@type": "xsd:float" }.
    /// </summary>
    public class WebVocabFloat
    {
        /// <summary>
        /// The numeric value. Typed as double because the mappers convert doubles natively in both directions.
        /// </summary>
        [OpenTraceabilityJson("@value")]
        public double Value { get; set; }

        /// <summary>
        /// The JSON-LD datatype of the value. Always "xsd:float" for GS1 Web Vocabulary floats.
        /// </summary>
        [OpenTraceabilityJson("@type")]
        public string Type { get; set; } = "xsd:float";
    }
}
