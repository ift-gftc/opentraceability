package interfaces

import java.lang.reflect.Type

interface IMasterDataMapper {
    fun Map(vocab: IVocabularyElement): String
    fun Map(T: Type, value: String): IVocabularyElement
    fun <T> Map(value: String): IVocabularyElement

}
