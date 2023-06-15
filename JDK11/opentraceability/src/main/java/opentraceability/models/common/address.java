package opentraceability.models.common;

import opentraceability.utility.Country;

public class Address extends AnonymizedAddress {

    public String address1 = null;
    public String address2 = null;
    public String city = null;
    public String state = null;
    public String county = null;
    @Override
    public String zipCode = null;
    @Override
    public Country country = null;

    @Override
    public String toString() {
        StringBuilder pieces = new StringBuilder();
        addIfNotNull(pieces, address1);
        addIfNotNull(pieces, city);
        addIfNotNull(pieces, state);
        addIfNotNull(pieces, county);
        addIfNotNull(pieces, zipCode);
        addIfNotNull(pieces, country != null ? country.abbreviation : null);

        String addressStr = pieces.toString();
        return addressStr;
    }

    private void addIfNotNull(StringBuilder sb, String s){
        if (s != null && !s.isBlank()) {
            if (sb.length() > 0){ sb.append(", "); }
            sb.append(s);
        }
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null){
            return false;
        }

        if (!(obj instanceof Address)){
            return false;
        }

        return this.equals((Address)obj);
    }

    public boolean equals(Address other) {

        if (other == null)
        {
            return false;
        }

        return (this.address1 == other.address1
                && this.address2 == other.address2
                && this.city == other.city
                && this.county == other.county
                && this.state == other.state
                && this.zipCode == other.zipCode
                && this.country == other.country);
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}