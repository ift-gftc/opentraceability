package models.masterdata
import java.util.*
import java.lang.reflect.Type
class Address {
    var Type: String = "gs1:PostalAddress"
    var Address1: List<LanguageString> = ArrayList<LanguageString>()
    var Address2: List<LanguageString> = ArrayList<LanguageString>()
    var City: List<LanguageString> = ArrayList<LanguageString>()
    var State: List<LanguageString> = ArrayList<LanguageString>()
    var Country: Country = Country()
    companion object{
    }
}
