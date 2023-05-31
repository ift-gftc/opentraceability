package interfaces

import org.json.JSONObject

interface IVocabularyElement {
    var ID: String?
    var EPCISType: String?
    var JsonLDType: String?
    var Context: JSONObject?
    var VocabularyType: VocabularyType
    var KDEs: ArrayList<IMasterDataKDE>
}
