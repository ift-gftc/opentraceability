package opentraceability.utility;

import opentraceability.OTLogger;
import org.w3c.dom.Element;

public class Country implements Comparable<Country> {

    public String cultureInfoCode = "";
    public String name = "";
    public String alternativeName = "";
    public String abbreviation = "";
    public String alpha3 = "";
    public int iso = 0;

    public Country() {}

    public Country(Country other) {
        this.abbreviation = other.abbreviation;
        this.alpha3 = other.alpha3;
        this.iso = other.iso;
        this.name = other.name;
        this.alternativeName = other.alternativeName;
        this.cultureInfoCode = other.cultureInfoCode;
    }

    public Country(Element xmlCountry) {
        this.name = xmlCountry.getAttribute("name") != null ? xmlCountry.getAttribute("name") : "";
        this.alternativeName = xmlCountry.getAttribute("alternativeName") != null ? xmlCountry.getAttribute("alternativeName") : "";
        this.abbreviation = xmlCountry.getAttribute("abbreviation") != null ? xmlCountry.getAttribute("abbreviation") : "";
        this.alpha3 = xmlCountry.getAttribute("alpha3") != null ? xmlCountry.getAttribute("alpha3") : "";
        String isoValue = xmlCountry.getAttribute("iso");
        this.iso = isoValue != null ? Integer.parseInt(isoValue) : 0;
        this.cultureInfoCode = xmlCountry.getAttribute("cultureInfoCode") != null ? xmlCountry.getAttribute("cultureInfoCode") : "";
    }

    public Country clone() {
        try {
            Country c = this;
            return c;
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    @Override
    public String toString() {
        return abbreviation.toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Country)){
            return false;
        }

        if (obj != null){
            return false;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return iso;
    }

    public boolean equals(Country other) {
        if (other == null) return false;
        return (iso == other.iso);
    }

    @Override
    public int compareTo(Country other) {
        if (other == null) return 1;
        return (Integer.compare(iso, other.iso));
    }
}