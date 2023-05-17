interface IEPCISQueryDocumentMapper {
    fun Map(String strValue, Boolean checkSchema): EPCISQueryDocument
    fun Map(EPCISQueryDocument doc): String
}
