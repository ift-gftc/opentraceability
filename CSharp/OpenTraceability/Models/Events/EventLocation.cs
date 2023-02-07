using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using GS1.Interfaces.Models.Events;
using GS1.Interfaces.Models.Identifiers;

namespace OpenTraceability.Models.Events
{
    public class EventLocation : IEventLocation
    {
        public EventLocation()
        {

        }

        public EventLocation(IGLN gln)
        {
            this.GLN = gln;
        }

        public IGLN GLN { get; set; }
    }
}
