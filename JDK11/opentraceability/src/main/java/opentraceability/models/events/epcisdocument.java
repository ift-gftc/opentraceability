package opentraceability.models.events;

public class EPCISDocument extends EPCISBaseDocument {

    public EPCISQueryDocument ToEPCISQueryDocument() {
        EPCISQueryDocument document = new EPCISQueryDocument();

        // get all properties from EPCISBaseDocument
        java.lang.reflect.Field[] props = EPCISBaseDocument.class.getDeclaredFields();

        // iterate over properties and copy their values to document
        for (java.lang.reflect.Field p : props) {
            p.setAccessible(true);
            Object v = p.get(this);
            p.set(document, v);
        }

        return document;
    }
}