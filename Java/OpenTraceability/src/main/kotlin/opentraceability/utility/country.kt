package utility

import OTLogger
import org.jdom2.Element
import javax.xml.bind.annotation.XmlElement

//[System.Xml.Serialization.XmlTypeAttribute(AnonymousType = true)
//[DataContract]
class Country :  Comparable<Country> {
    var CultureInfoCode: String = ""
    var Name: String = ""
    var AlternativeName: String = ""
    var Abbreviation: String = ""
    var Alpha3: String = ""
    var ISO: Int = 0

    constructor(other: Country) {
        this.Abbreviation = other.Abbreviation
        this.Alpha3 = other.Alpha3
        this.ISO = other.ISO
        this.Name = other.Name
        this.AlternativeName = other.AlternativeName
        this.CultureInfoCode = other.CultureInfoCode
    }

    constructor(xmlCountry: Element) {
        this.Name = xmlCountry.getAttribute("Name") ?: ""
        this.AlternativeName = xmlCountry.getAttribute("AlternativeName") ?: ""
        this.Abbreviation = xmlCountry.getAttribute("Abbreviation") ?: ""
        this.Alpha3 = xmlCountry.getAttribute("Alpha3") ?: ""
        val isoValue = xmlCountry.getAttribute("ISO")
        this.ISO = isoValue?.toIntOrNull() ?: 0
        this.CultureInfoCode = xmlCountry.getAttribute("CultureInfoCode") ?: ""
    }

    fun clone(): Country {
        try {
            var c: Country = this
            return c
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }


    override fun toString(): String {
        return this.Abbreviation.toString()
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
        return this.ISO.hashCode()
    }

    fun equals(other: Country?): Boolean {
        if (other == null) return false
        return (ISO == other.ISO)
    }
    override fun compareTo(other: Country): Int {
        if (other == null) return 1;
        return (ISO.compareTo(other.ISO));
    }


}
