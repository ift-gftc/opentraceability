package models.masterdata
import com.fasterxml.jackson.core.JsonToken
import java.util.*
import models.identifiers.*
import models.events.kdes.CertificationList
import java.lang.reflect.Type
class Location {
    var ID: String = String()
    var EPCISType: String = "urn:epcglobal:epcis:vtype:Location"
    var JsonLDType: String = "gs1:Place"
    var VocabularyType: VocabularyType = VocabularyType()
    var Context: JsonToken = JsonToken()
    var GLN: GLN = GLN()
    var OwningParty: PGLN = PGLN()
    var InformationProvider: PGLN = PGLN()
    var CertificationList: CertificationList = CertificationList()
    var Name: List<LanguageString> = ArrayList<LanguageString>()
    var Address: Address = Address()
    var UnloadingPort: String = String()
    var KDEs: List<IMasterDataKDE> = ArrayList<IMasterDataKDE>()
    companion object{
    }
}
