package models.masterdata

import com.fasterxml.jackson.core.JsonToken
import interfaces.IMasterDataKDE
import interfaces.VocabularyType
import java.util.*
import models.identifiers.*
import java.lang.reflect.Type

class VocabularyElement {
    var ID: String = String()
    var EPCISType: String = String()
    var JsonLDType: String = String()
    var Context: JsonToken = JsonToken()
    var VocabularyType: VocabularyType = VocabularyType()
    var KDEs: List<IMasterDataKDE> = ArrayList<IMasterDataKDE>()

    companion object {
    }
}
