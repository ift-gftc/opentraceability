package models.masterdata


import interfaces.IMasterDataKDE
import interfaces.VocabularyType
import java.util.*
import models.identifiers.*
import utility.EnumUtil
import java.lang.reflect.Type

class VocabularyElement : IVocabularyElement {
    var ID: String? = null
    var epcisType: String? = null
    var JsonLDType: String? = null
    var Context: JSONObject? = null

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
