package opentraceability.models.common;

import opentraceability.utility.Country;

public class AnonymizedAddress {
    public String zipCode = null;
    public Country country = null;

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof AnonymizedAddress)) {
            return false;
        }

        return this.equals((AnonymizedAddress) obj);
    }

    public boolean equals(AnonymizedAddress other) {
        if (other == null) {
            return false;
        }

        return (this.zipCode == other.zipCode && this.country == other.country);
    }

    public int hashCode() {
        return this.zipCode.hashCode() + this.country.hashCode();
    }
}