package opentraceability.models.events

class EPCISDocument : EPCISBaseDocument() {

    fun ToEPCISQueryDocument(): EPCISQueryDocument {
        val document = EPCISQueryDocument()

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
