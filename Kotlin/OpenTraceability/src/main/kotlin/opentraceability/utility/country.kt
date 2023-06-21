package opentraceability.utility

import opentraceability.OTLogger
import org.w3c.dom.Element

//[System.Xml.Serialization.XmlTypeAttribute(AnonymousType = true)
//[DataContract]
class Country :  Comparable<Country> {
    var cultureInfoCode: String = ""
    var name: String = ""
    var alternativeName: String = ""
    var abbreviation: String = ""
    var alpha3: String = ""
    var iso: Int = 0

    constructor(){}

    constructor(other: Country) {
        this.abbreviation = other.abbreviation
        this.alpha3 = other.alpha3
        this.iso = other.iso
        this.name = other.name
        this.alternativeName = other.alternativeName
        this.cultureInfoCode = other.cultureInfoCode
    }

    constructor(xmlCountry: Element) {
        this.name = xmlCountry.getAttribute("name") ?: ""
        this.alternativeName = xmlCountry.getAttribute("alternativeName") ?: ""
        this.abbreviation = xmlCountry.getAttribute("abbreviation") ?: ""
        this.alpha3 = xmlCountry.getAttribute("alpha3") ?: ""
        val isoValue = xmlCountry.getAttribute("iso")
        this.iso = isoValue?.toIntOrNull() ?: 0
        this.cultureInfoCode = xmlCountry.getAttribute("cultureInfoCode") ?: ""
    }

    fun clone(): Country {
        try {
            var c: Country = this
            return c
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }


    override fun toString(): String {
        return this.abbreviation.toString()
    }


    override fun equals(obj: Any?): Boolean {

        if (!(obj is Country)){
            return false
        }

        if (obj != null){
            return false
        }

        return false
    }

    override fun hashCode(): Int {
        return this.iso.hashCode()
    }

    fun equals(other: Country?): Boolean {
        if (other == null) return false
        return (iso == other.iso)
    }
    override fun compareTo(other: Country): Int {
        if (other == null) return 1;
        return (iso.compareTo(other.iso));
    }


}
