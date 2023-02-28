using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.GDST.Events
{
    /// <summary>
    /// This is a GDST event that has an ILMD property.
    /// </summary>
    public interface IGDSTILMDEvent : IGDSTEvent
    {
        GDSTILMD? ILMD { get; }
    }
}
