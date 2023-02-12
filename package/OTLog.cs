using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability
{
    /// <summary>
    /// Represents a log in the Open Traceability logger.
    /// </summary>
    public class OTLog
    {
        public LogLevel Level { get; internal set; }
        public Exception Exception { get; internal set; }
    }
}
