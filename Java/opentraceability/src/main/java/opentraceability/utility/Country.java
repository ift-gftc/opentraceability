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
        this.name = xmlCountry.getAttribute("Name") != null ? xmlCountry.getAttribute("Name") : "";
        this.alternativeName = xmlCountry.getAttribute("AlternativeName") != null ? xmlCountry.getAttribute("AlternativeName") : "";
        this.abbreviation = xmlCountry.getAttribute("Abbreviation") != null ? xmlCountry.getAttribute("Abbreviation") : "";
        this.alpha3 = xmlCountry.getAttribute("Alpha3") != null ? xmlCountry.getAttribute("Alpha3") : "";
        String isoValue = xmlCountry.getAttribute("ISO");
        this.iso = isoValue != null ? Integer.parseInt(isoValue) : 0;
        this.cultureInfoCode = xmlCountry.getAttribute("CultureInfoCode") != null ? xmlCountry.getAttribute("CultureInfoCode") : "";
    }

    public Country clone() {
        Country c = this;
        return c;
    }

    @Override
    public String toString() {
        return abbreviation;
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