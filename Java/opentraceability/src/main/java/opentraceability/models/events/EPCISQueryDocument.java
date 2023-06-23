package opentraceability.models.events;

public class EPCISQueryDocument extends EPCISBaseDocument {
    public String QueryName = "";
    public String SubscriptionID = "";

    public EPCISDocument ToEPCISDocument() {
        EPCISDocument document = new EPCISDocument();

        // get all properties from EPCISBaseDocument
        java.lang.reflect.Field[] props = EPCISBaseDocument.class.getDeclaredFields();

        // iterate over properties and copy their values to document
        for (java.lang.reflect.Field p : props) {
            p.setAccessible(true);
            try {
                Object v = p.get(this);
                p.set(document, v);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return document;
    }
}