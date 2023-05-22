package interfaces

import java.lang.reflect.Type

interface IMasterDataMapper {
    fun Map(vocab: IVocabularyElement): String
    fun <T> Map(Type: T, value: String): IVocabularyElement
    fun <T> Map(value: String): IVocabularyElement
}
