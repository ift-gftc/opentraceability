using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using GS1.Interfaces.Models.Events;
using GS1.Interfaces.Models.Identifiers;
using DSUtil;

namespace OpenTraceability.Models.Events
{
    public class EventSource : IEventSource
    {
        public string RawType { get; set; }

        public EventSourceType Type
        {
            get
            {
                EventSourceType type = EventSourceType.Unknown;
                foreach (EventSourceType t in Enum.GetValues(typeof(EventSourceType)))
                {
                    if (DSEnumUtil.GetEnumDescription(t).Trim().ToLower() == RawType.Trim().ToLower())
                    {
                        type = t;
                    }
                }
                return type;
            }
        }

        public string Value { get; set; }

        public EventSource()
        {

        }

        public EventSource(PGLN pgln, EventSourceType type)
        {
            if (type != EventSourceType.Owner && type != EventSourceType.Possessor)
            {
                throw new Exception("When constructing a EventSource from a PGLN, the EventSourceType must either be Owner or Possessor.");
            }

            this.RawType = DSEnumUtil.GetEnumDescription(type);
            this.Value = pgln.ToString();
        }
    }
}
