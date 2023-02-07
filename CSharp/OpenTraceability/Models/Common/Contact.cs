using GS1.Interfaces.Models.Common;
using GS1.StaticData;
using System;
using System.Collections.Generic;
using System.Text;

namespace OpenTraceability.Models.Common
{
    public class Contact
    {
        public string ContactType { get; set; }
        public string ContactTitle { get; set; }
        public string Responsibility { get; set; }
        public string Name { get; set; }
        public string Email { get; set; }
        public string Phone { get; set; }
        public string Fax { get; set; }
    }
}
