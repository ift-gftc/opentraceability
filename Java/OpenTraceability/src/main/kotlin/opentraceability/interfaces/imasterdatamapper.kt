package interfaces

import java.lang.reflect.Type
import kotlin.reflect.KClass

interface IMasterDataMapper {
    fun Map(vocab: IVocabularyElement): String
    fun <T: IVocabularyElement> Map(t: Class<T>, value: String): IVocabularyElement
     fun < T: IVocabularyElement> Map(value: String): IVocabularyElement
}
