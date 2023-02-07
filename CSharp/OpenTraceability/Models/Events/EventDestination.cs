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
    public class EventDestination : IEventDestination
    {
        public string RawType { get; set; }

        public EventDestinationType Type
        {
            get
            {
                EventDestinationType type = EventDestinationType.Unknown;
                foreach (EventDestinationType t in Enum.GetValues(typeof(EventDestinationType)))
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

        public EventDestination()
        {

        }

        public EventDestination(PGLN pgln, EventDestinationType type)
        {
            if (type != EventDestinationType.Owner && type != EventDestinationType.Possessor)
            {
                throw new Exception("When constructing a EventDestination from a PGLN, the EventDestinationType must either be Owner or Possessor.");
            }

            this.RawType = DSEnumUtil.GetEnumDescription(type);
            this.Value = pgln.ToString();
        }
    }
}
