package models.masterdata
import com.fasterxml.jackson.core.JsonToken
import interfaces.IMasterDataKDE
import interfaces.VocabularyType
import models.common.LanguageString
import java.util.*
import models.identifiers.*
import java.lang.reflect.Type
class TradingParty {
    var ID: String = String()
    var EPCISType: String = "urn:epcglobal:epcis:vtype:Party"
    var JsonLDType: String = "gs1:Organization"
    var VocabularyType: VocabularyType = VocabularyType()
    var Context: JsonToken = JsonToken()
    var PGLN: PGLN = PGLN()
    var OwningParty: PGLN = PGLN()
    var InformationProvider: PGLN = PGLN()
    var Name: List<LanguageString> = ArrayList<LanguageString>()
    var Address: Address = Address()
    var IFTP: String = String()
    var KDEs: List<IMasterDataKDE> = ArrayList<IMasterDataKDE>()
    companion object{
    }
}
