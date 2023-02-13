using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System.ComponentModel;

namespace OpenTraceability.Models.Events
{
    public enum EventSourceType
    {
        Unknown = 0,

        [Description("urn:epcglobal:cbv:sdt:owning_party")]
        Owner = 1,

        [Description("urn:epcglobal:cbv:sdt:possessing_party")]
        Possessor = 2,

        [Description("urn:epcglobal:cbv:sdt:location")]
        Location = 3
    }

    [OpenTraceabilityXml("source")]
    public class EventSource
    {
        [OpenTraceabilityXml("@type")]
        public string RawType { get; set; } = string.Empty;

        public EventSourceType Type
        {
            get
            {
                EventSourceType type = EventSourceType.Unknown;
                foreach (EventSourceType t in Enum.GetValues(typeof(EventSourceType)))
                {
                    if (EnumUtil.GetEnumDescription(t).Trim().ToLower() == RawType?.Trim().ToLower())
                    {
                        type = t;
                    }
                }
                return type;
            }
        }

        [OpenTraceabilityXml("text()")]
        public string? Value { get; set; }

        public EventSource()
        {

        }
    }
}