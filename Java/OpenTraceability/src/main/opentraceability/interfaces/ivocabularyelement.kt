package interfaces

import com.fasterxml.jackson.core.JsonToken

interface IVocabularyElement {
    var ID: String?
    var EPCISType: String?
    var JsonLDType: String?
    var Context: JsonToken?
    var VocabularyType: VocabularyType
    var KDEs: ArrayList<IMasterDataKDE>
}
