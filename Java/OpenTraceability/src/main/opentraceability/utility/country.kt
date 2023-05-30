package utility

import OTLogger
import javax.xml.bind.annotation.XmlElement

//[System.Xml.Serialization.XmlTypeAttribute(AnonymousType = true)
//[DataContract]
class Country : IEquatable<Country>, IComparable<Country> {
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

    constructor(xmlCountry: XmlElement) {
        this.Name = xmlCountry.attribute("Name")?.value ?: ""
        this.AlternativeName = xmlCountry.attribute("AlternativeName")?.value ?: ""
        this.Abbreviation = xmlCountry.attribute("Abbreviation")?.value ?: ""
        this.Alpha3 = xmlCountry.attribute("Alpha3")?.value ?: ""
        val isoValue = xmlCountry.attribute("ISO")?.value
        this.ISO = isoValue?.toIntOrNull() ?: 0
        this.CultureInfoCode = xmlCountry.attribute("CultureInfoCode")?.value ?: ""

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
    fun compareTo(other: Country?): Int {
        if (other == null) return 1;
        return (ISO.compareTo(other.ISO));
    }
}
