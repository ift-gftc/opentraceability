package opentraceability.interfaces;



public interface IMasterDataMapper {
    String map(IVocabularyElement vocab) throws Exception;
    IVocabularyElement map(Class t, String value) throws Exception;
}