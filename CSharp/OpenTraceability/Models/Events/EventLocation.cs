using OpenTraceability.Models.Identifiers;

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

        public GLN? GLN { get; set; }
    }
}