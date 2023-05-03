using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System.ComponentModel;

namespace OpenTraceability.Models.Events
{
    public enum EventSourceType
    {
        Unknown = 0,

        [CBV("owning_party")]
        [CBV("https://ref.gs1.org/cbv/SDT-owning_party")]
        [CBV("urn:epcglobal:cbv:sdt:owning_party")]
        Owner = 1,

        [CBV("possessing_party")]
        [CBV("https://ref.gs1.org/cbv/SDT-possessing_party")]
        [CBV("urn:epcglobal:cbv:sdt:possessing_party")]
        Possessor = 2,

        [CBV("location")]
        [CBV("https://ref.gs1.org/cbv/SDT-location")]
        [CBV("urn:epcglobal:cbv:sdt:location")]
        Location = 3
    }

    public class EventSource
    {
        [OpenTraceabilityJson("type")]
        [OpenTraceability("@type")]
        public Uri? Type { get; set; }

        public EventSourceType ParsedType
        {
            get
            {
                EventSourceType type = EventSourceType.Unknown;

                foreach (var e in Enum.GetValues<EventSourceType>())
                {
                    if (EnumUtil.GetEnumAttributes<CBVAttribute>(e).Exists(e => e.Value.ToLower() == Type?.ToString().ToLower()))
                    {
                        return e;
                    }
                }

                return type;
            }
            set
            {
                string? t = EnumUtil.GetEnumAttributes<CBVAttribute>(value).Where(e => e.Value.StartsWith("urn")).FirstOrDefault()?.Value;
                if (!string.IsNullOrWhiteSpace(t))
                {
                    this.Type = new Uri(t);
                }
            }
        }

        [OpenTraceabilityJson("source")]
        [OpenTraceability("text()")]
        public string? Value { get; set; }

        public EventSource()
        {

        }
    }
}