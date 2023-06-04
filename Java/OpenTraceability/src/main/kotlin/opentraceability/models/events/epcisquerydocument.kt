package opentraceability.models.events

class EPCISQueryDocument : EPCISBaseDocument() {
    var QueryName: String = ""
    var SubscriptionID: String = ""

    fun ToEPCISDocument(): EPCISDocument {
        val document = EPCISDocument()

        // get all properties from EPCISBaseDocument
        val props = EPCISBaseDocument::class.java.declaredFields

        // iterate over properties and copy their values to document
        for (p in props) {
            p.isAccessible = true
            val v = p.get(this)
            p.set(document, v)
        }

        return document
    }
}
