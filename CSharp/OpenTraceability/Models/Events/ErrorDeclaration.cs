using OpenTraceability.Utility;
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
        public string? RawReason { get; set; }
        public DateTime? DeclarationTime { get; set; }
        public List<string>? CorrectingEventIDs { get; set; }

        public EventErrorType Reason
        {
            get
            {
                EventErrorType type = EventErrorType.Unknown;
                foreach (EventErrorType t in Enum.GetValues(typeof(EventErrorType)))
                {
                    if (EnumUtil.GetEnumDescription(t) == RawReason)
                    {
                        type = t;
                    }
                }
                return type;
            }
            set
            {
                string reason = EnumUtil.GetEnumDescription(value);
                this.RawReason = reason;
            }
        }
    }
}