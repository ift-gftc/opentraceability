package models.masterdata

import com.fasterxml.jackson.core.JsonToken
import interfaces.IMasterDataKDE
import interfaces.VocabularyType
import java.util.*
import models.identifiers.*
import java.lang.reflect.Type

class VocabularyElement : IVocabularyElement {
    var ID: String? = null
    var EPCISType: String? = null
    var JsonLDType: String? = null
    var Context: JsonToken? = null

    val vocabularyType: VocabularyType
        get() {
            var type = VocabularyType.Unknown
            for (t in VocabularyType.values()) {
                if (EnumUtil.getEnumDescription(t).trim().toLowerCase() == epcisType?.trim().toLowerCase()) {
                    type = t
                }
            }
            return type
        }



    var KDEs: ArrayList<IMasterDataKDE> = ArrayList<IMasterDataKDE>()

}
