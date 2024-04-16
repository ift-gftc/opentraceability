using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability
{
    public enum LogLevel
    {
        Info = 0,
        Warning = 1,
        Error = 2,
        Debug = 3
    }

    public delegate void OnLogDelegate(OTLog log);

    /// <summary>
    /// All logs are sent here, and then handled via the attached logger to the program. This allows for swapping of logging services
    /// easily.
    /// </summary>
    public static class OTLogger
    {
        /// <summary>
        /// Attach to this to see all logs in the open traceability libraries.
        /// </summary>
        public static event OnLogDelegate OnLog;

        /// <summary>
        /// Called when an exception occurs that is an error in the library logic.
        /// </summary>
        /// <param name="ex">The exception that was thrown.</param>
        public static void Error(Exception ex)
        {
            if (OnLog != null)
            {
                OTLog log = new OTLog();
                log.Level = LogLevel.Error;
                log.Exception = ex;
            }
        }
    }
}
