using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System.ComponentModel;

namespace OpenTraceability.Models.Events
{
    public enum EventErrorType
    {
        Unknown = 0,

        [Description("urn:epcis:errorType:incorrect_data")]
        IncorrectData = 1,

        [Description("urn:epcis:errorType:did_not_occur")]
        DidNotOccur = 2
    }

    public class ErrorDeclaration
    {
        [OpenTraceabilityJson("reason")]
        [OpenTraceability("@type")]
        public Uri? RawReason { get; set; }

        [OpenTraceability("declarationTime")]
        public DateTimeOffset? DeclarationTime { get; set; }

        [OpenTraceabilityArray("correctiveEventID")]
        [OpenTraceability("correctiveEventIDs")]
        public List<string>? CorrectingEventIDs { get; set; }

        public EventErrorType Reason
        {
            get
            {
                EventErrorType type = EventErrorType.Unknown;
                foreach (EventErrorType t in Enum.GetValues(typeof(EventErrorType)))
                {
                    if (EnumUtil.GetEnumDescription(t) == RawReason?.ToString())
                    {
                        type = t;
                    }
                }
                return type;
            }
            set
            {
                string reason = EnumUtil.GetEnumDescription(value);
                this.RawReason = new Uri(reason);
            }
        }
    }
}