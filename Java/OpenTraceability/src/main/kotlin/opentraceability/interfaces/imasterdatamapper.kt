package opentraceability.interfaces

import java.lang.reflect.Type
import kotlin.reflect.KClass

interface IMasterDataMapper {
    fun map(vocab: IVocabularyElement): String
    fun <T: IVocabularyElement> map(t: KClass<T>, value: String): IVocabularyElement
    fun <T: IVocabularyElement> map(value: String): IVocabularyElement
}
