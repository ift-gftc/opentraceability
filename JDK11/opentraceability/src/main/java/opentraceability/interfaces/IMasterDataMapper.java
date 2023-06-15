package opentraceability.interfaces;

import java.lang.reflect.Type;

public interface IMasterDataMapper {
    String map(IVocabularyElement vocab);
    IVocabularyElement map(Type t, String value);
}