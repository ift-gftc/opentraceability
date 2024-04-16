namespace OpenTraceability.Models.Common
{
    public class Address : AnonymizedAddress
    {
        public string Address1 { get; set; }
        public string Address2 { get; set; }
        public string City { get; set; }
        public string State { get; set; }
        public string County { get; set; }

        public override string ToString()
        {
            List<string> pieces = new List<string>()
            {
                Address1,
                Address2,
                City,
                County,
                State,
                ZipCode,
                Country?.Abbreviation
            };
            string addressStr = string.Join(", ", pieces.Where(p => !string.IsNullOrWhiteSpace(p)));
            return addressStr;
        }

        public override bool Equals(object obj)
        {
            if (obj == null)
            {
                return false;
            }

            if (!(obj is Address))
            {
                return false;
            }

            return this.Equals(obj as Address);
        }

        public bool Equals(Address other)
        {
            if (other == null)
            {
                return false;
            }

            return (this.Address1 == other.Address1
                && this.Address2 == other.Address2
                && this.City == other.City
                && this.County == other.County
                && this.State == other.State
                && this.ZipCode == other.ZipCode
                && this.Country == other.Country);
        }

        public override int GetHashCode()
        {
            return this.ToString().GetHashCode();
        }
    }
}