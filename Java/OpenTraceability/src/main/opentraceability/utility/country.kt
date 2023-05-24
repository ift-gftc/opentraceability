package utility

class Country {
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

    fun Clone(): Country{
        TODO("Not yet implemented")
    }


    fun ToString(): String{
        TODO("Not yet implemented")
    }
}
