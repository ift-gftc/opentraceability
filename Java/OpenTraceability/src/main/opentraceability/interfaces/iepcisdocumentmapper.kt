interface IEPCISDocumentMapper {
    fun Map(strValue: String): EPCISDocument
    fun Map(doc: EPCISDocument): String
}
