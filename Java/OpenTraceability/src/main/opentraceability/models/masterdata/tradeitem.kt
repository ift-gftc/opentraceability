package models.masterdata
import com.fasterxml.jackson.core.JsonToken
import java.util.*
import models.identifiers.*
import java.lang.reflect.Type
class Tradeitem {
    var ID: String = String()
    var EPCISType: String = "urn:epcglobal:epcis:vtype:EPCClass"
    var JsonLDType: String = "gs1:Product"
    var VocabularyType: VocabularyType = VocabularyType()
    var Context: JsonToken = JsonToken()
    var GTIN: GTIN = GTIN()
    var ShortDescription: List<LanguageString> = ArrayList<LanguageString>()
    var TradeItemConditionCode: String = String()
    var OwningParty: PGLN = PGLN()
    var InformationProvider: PGLN = PGLN()
    var FisherySpeciesScientificName: List<String> = ArrayList<String>()
    var FisherySpeciesCode: List<String> = ArrayList<String>()
    var KDEs: List<IMasterDataKDE> = ArrayList<IMasterDataKDE>()
    companion object{
    }
}
