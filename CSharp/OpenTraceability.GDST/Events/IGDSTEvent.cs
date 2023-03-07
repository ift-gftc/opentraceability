using OpenTraceability.Interfaces;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.GDST.Events
{
    public interface IGDSTEvent : IEvent
    {
        PGLN? ProductOwner { get; set; }
        PGLN? InformationProvider { get; set; }
    }
}
