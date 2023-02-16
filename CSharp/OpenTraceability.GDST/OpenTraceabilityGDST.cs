using OpenTraceability.GDST.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.GDST
{
    public static class OpenTraceabilityGDST
    {
        public static void Initialize()
        {
            OpenTraceability.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(GDSTFishingEvent), "ObjectEvent", "urn:gdst:bizStep:fishingEvent", Models.Events.EventAction.ADD));
        }
    }
}
