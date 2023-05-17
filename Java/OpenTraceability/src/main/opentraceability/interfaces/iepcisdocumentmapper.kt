interface IEPCISDocumentMapper {
    fun Map(String strValue): EPCISDocument
    fun Map(EPCISDocument doc): String
}
