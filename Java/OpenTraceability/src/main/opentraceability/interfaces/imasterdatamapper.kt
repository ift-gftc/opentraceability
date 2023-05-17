interface IMasterDataMapper {
    fun Map(IVocabularyElement vocab): String
    fun Map(Type T, String value): IVocabularyElement
    fun Map(String value): IVocabularyElement
}
