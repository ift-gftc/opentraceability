using OpenTraceability.Utility;

namespace OpenTraceability.Models.Common
{
    public class AnonymizedAddress
    {
        public string ZipCode { get; set; }

        public Country Country { get; set; }

        public override bool Equals(object obj)
        {
            if (obj == null)
            {
                return false;
            }

            if (!(obj is AnonymizedAddress))
            {
                return false;
            }

            return this.Equals(obj as AnonymizedAddress);
        }

        public bool Equals(AnonymizedAddress other)
        {
            if (other == null)
            {
                return false;
            }

            return (this.ZipCode == other.ZipCode && this.Country == other.Country);
        }

        public override int GetHashCode()
        {
            return (this.ZipCode?.GetHashCode() ?? 0) + (this.Country?.GetHashCode() ?? 0);
        }
    }
}