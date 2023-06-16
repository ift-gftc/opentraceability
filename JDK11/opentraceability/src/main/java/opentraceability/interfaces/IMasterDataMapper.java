package opentraceability.interfaces;

import java.lang.reflect.Type;

public interface IMasterDataMapper {
    String map(IVocabularyElement vocab) throws Exception;
    IVocabularyElement map(Type t, String value) throws Exception;
}