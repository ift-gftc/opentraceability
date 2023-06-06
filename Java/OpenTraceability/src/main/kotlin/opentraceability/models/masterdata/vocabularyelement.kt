package opentraceability.models.masterdata

import opentraceability.interfaces.IMasterDataKDE
import opentraceability.interfaces.IVocabularyElement
import opentraceability.interfaces.VocabularyType
import opentraceability.models.identifiers.*
import opentraceability.utility.EnumUtil
import java.util.*
import org.json.JSONObject

class VocabularyElement() : IVocabularyElement {
    override var id: String? = null
    override var epcisType: String? = ""
    override var jsonLDType: String? = null
    override var context: JSONObject? = null

    override var vocabularyType: VocabularyType = VocabularyType.Unknown
        get() {
            var type = VocabularyType.Unknown
            for (t in VocabularyType.values()) {
                if (EnumUtil.GetEnumDescription<VocabularyType>(t)?.trim() == epcisType?.trim()) {
                    type = t
                }
            }
            return type
        }
        set(value) {
            field = value
        }


    override var kdes: MutableList<IMasterDataKDE> = mutableListOf()

}
