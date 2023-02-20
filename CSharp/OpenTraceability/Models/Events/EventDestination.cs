using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System.ComponentModel;

namespace OpenTraceability.Models.Events
{
    public enum EventDestinationType
    {
        Unknown = 0,

        [Description("urn:epcglobal:cbv:sdt:owning_party")]
        Owner = 1,

        [Description("urn:epcglobal:cbv:sdt:possessing_party")]
        Possessor = 2,

        [Description("urn:epcglobal:cbv:sdt:location")]
        Location = 3
    }

    public class EventDestination
    {
        [OpenTraceabilityJson("type")]
        [OpenTraceability("@type")]
        public string RawType { get; set; } = string.Empty;

        public EventDestinationType Type
        {
            get
            {
                EventDestinationType type = EventDestinationType.Unknown;
                foreach (EventDestinationType t in Enum.GetValues(typeof(EventDestinationType)))
                {
                    if (EnumUtil.GetEnumDescription(t).Trim().ToLower() == RawType.Trim().ToLower())
                    {
                        type = t;
                    }
                }
                return type;
            }
        }

        [OpenTraceabilityJson("destination")]
        [OpenTraceability("text()")]
        public string? Value { get; set; }

        public EventDestination()
        {

        }
    }
}