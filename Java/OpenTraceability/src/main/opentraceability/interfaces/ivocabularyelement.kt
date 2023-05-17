interface IVocabularyElement {
    fun get_ID(): String
    fun get_EPCISType(): String
    fun set_EPCISType(String value): Void
    fun get_JsonLDType(): String
    fun set_JsonLDType(String value): Void
    fun get_Context(): JToken
    fun set_Context(JToken value): Void
    fun get_VocabularyType(): VocabularyType
    fun get_KDEs(): List<IMasterDataKDE>
    fun set_KDEs(List<IMasterDataKDE> value): Void
}
