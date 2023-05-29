package opentraceability.mappers.masterdata

import com.fasterxml.jackson.core.JsonToken
import opentraceability.interfaces.IMasterDataMapper
import opentraceability.interfaces.IVocabularyElement
import java.lang.reflect.Type
import java.util.*

class GS1VocabJsonMapper : IMasterDataMapper {

    override fun Map(vocab: IVocabularyElement): String {
        TODO("Not yet implemented")
    }

    override fun <T> Map(value: String): IVocabularyElement {
        TODO("Not yet implemented")
    }

    override fun Map(t: Type, value: String): IVocabularyElement {
        TODO("Not yet implemented")
    }


    internal fun GetNamespaces(jContext: JsonToken) : MutableMap<String, String> {
        TODO("Not yet implemented")
    }

}
