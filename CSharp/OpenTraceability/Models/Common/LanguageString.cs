using Newtonsoft.Json;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Models.Common
{
    /// <summary>
    /// Represents a string in a particular langauge.
    /// </summary>
    public class LanguageString
    {
        [JsonProperty("@language")]
        public string Language { get; set; } = string.Empty;

        [JsonProperty("@value")]
        public string Value { get; set; } = string.Empty;
    }
}
