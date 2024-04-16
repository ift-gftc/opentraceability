using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    public class EventLocation
    {
        public EventLocation()
        {

        }

        public EventLocation(GLN gln)
        {
            this.GLN = gln;
        }

        [OpenTraceability("id")]
        public GLN GLN { get; set; }
    }
}