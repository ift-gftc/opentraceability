package opentraceability.interfaces

import org.json.JSONObject

interface IVocabularyElement {
    var id: String?
    var epcisType: String?
    var jsonLDType: String?
    var context: JSONObject?
    var vocabularyType: VocabularyType
    var kdes: MutableList<IMasterDataKDE>
}
