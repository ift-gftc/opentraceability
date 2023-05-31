package models.masterdata


import interfaces.IMasterDataKDE
import interfaces.IVocabularyElement
import interfaces.VocabularyType
import java.util.*
import models.identifiers.*
import org.json.JSONObject
import utility.EnumUtil
import java.lang.reflect.Type

class VocabularyElement : IVocabularyElement {
    override var ID: String? = null
    override var EPCISType: String? = ""
    override var JsonLDType: String? = null
    override var Context: JSONObject? = null

    var VocabularyType: VocabularyType
        get() {
            var type = VocabularyType.Unknown
            for (t in VocabularyType.values()) {
                if (EnumUtil.getEnumDescription(t).trim().toLowerCase() == EPCISType?.trim().toLowerCase()) {
                    type = t
                }
            }
            return type
        }
        set(value) {
            field = value
        }


    override var KDEs: ArrayList<IMasterDataKDE> = ArrayList<IMasterDataKDE>()

}
