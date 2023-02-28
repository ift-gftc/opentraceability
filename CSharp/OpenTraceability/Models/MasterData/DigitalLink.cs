using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Models.MasterData
{
    public class DigitalLink
    {
        public string link { get; set; } = string.Empty;
        public string title { get; set; } = string.Empty;
        public string linkType { get; set; } = string.Empty;
        public string ianaLanguage { get; set; } = string.Empty;
        public string context { get; set; } = string.Empty;
        public string mimeType { get; set; } = string.Empty;
        public bool active { get; set; }
        public bool fwqs { get; set; }
        public bool defaultLinkType { get; set; }
        public bool defaultIanaLanguage { get; set; }
        public bool defaultContext { get; set; }
        public bool defaultMimeType { get; set; }
        public string identifier { get; set; } = string.Empty;
        public bool authRequired { get; set; }
    }
}
