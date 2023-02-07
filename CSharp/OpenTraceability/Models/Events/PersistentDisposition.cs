using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using GS1.Interfaces.Models.Events;

namespace OpenTraceability.Models.Events
{
    public class PersistentDisposition : IPersistentDisposition
    {
        public List<string> Set { get; set; }
        public List<string> Unset { get; set; }
    }
}
