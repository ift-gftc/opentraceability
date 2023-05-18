package interfaces
import java.lang.reflect.Type
interface IMasterDataMapper {
    fun Map(vocab: IVocabularyElement): String
    fun Map(T: Type, value: String): IVocabularyElement
    fun Map(value: String): IVocabularyElement
}
