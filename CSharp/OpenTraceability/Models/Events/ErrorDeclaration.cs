using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using GS1.Interfaces.Models.Events;
using DSUtil;

namespace OpenTraceability.Models.Events
{
    public class ErrorDeclaration : IErrorDeclaration
    {
        public string RawReason { get; set; }
        public DateTime? DeclarationTime { get; set; }
        public List<string> CorrectingEventIDs { get; set; }
        public EventErrorType Reason
        {
            get
            {
                EventErrorType type = EventErrorType.Unknown;
                foreach (EventErrorType t in Enum.GetValues(typeof(EventErrorType)))
                {
                    if (DSEnumUtil.GetEnumDescription(t) == RawReason)
                    {
                        type = t;
                    }
                }
                return type;
            }
            set
            {
                string reason = DSEnumUtil.GetEnumDescription(value);
                this.RawReason = reason;
            }
        }
    }
}
