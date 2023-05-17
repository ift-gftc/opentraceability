import java.lang.reflect.Type
interface IVocabularyElement {
    fun get_ID(): String
    fun get_EPCISType(): String
    fun set_EPCISType(value: String): Void
    fun get_JsonLDType(): String
    fun set_JsonLDType(value: String): Void
    fun get_Context(): JToken
    fun set_Context(value: JToken): Void
    fun get_VocabularyType(): VocabularyType
    fun get_KDEs(): List<IMasterDataKDE>
    fun set_KDEs(value: List<IMasterDataKDE>): Void
}
