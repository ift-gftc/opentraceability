using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Models.Events.KDEs
{
    /// <summary>
    /// Base class of the Event KDE.
    /// </summary>
    public class EventKDEBase
    {
        public string Key { get; protected set; } = string.Empty;
    }
}
