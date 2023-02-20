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

    public class EventSource
    {
        [OpenTraceabilityJson("type")]
        [OpenTraceability("@type")]
        public Uri? Type { get; set; }

        [OpenTraceabilityJson("source")]
        [OpenTraceability("text()")]
        public string? Value { get; set; }

        public EventSource()
        {

        }
    }
}