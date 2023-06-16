package opentraceability.models.events.kdes;

import opentraceability.interfaces.IEventKDE;
import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Type;
import java.util.Map;

public class EventKDEString extends IEventKDE {
    public Type valueType = String.class;
    public String value;
    public String type;
    public Map<String, String> attributes;

    public EventKDEString() {}

    public EventKDEString(String ns, String name) {
        this.namespace = ns;
        this.name = name;
    }

    @Override
    public JSONObject getJson() {
        if (value == null || value.isEmpty()) {
            return null;
        } else {
            JSONObject json = new JSONObject();
            json.put("keyName", value);
            return json;
        }
    }

    @Override
    public Element getXml() throws Exception {
        if (value == null || value.isEmpty()) {
            return null;
        } else {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            Document document = docFactory.newDocumentBuilder().newDocument();

            Element element = document.createElementNS(namespace, name);
            element.setTextContent(value);

            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                Attr attr = document.createAttributeNS(namespace, entry.getKey());
                attr.setValue(entry.getValue());
                element.setAttributeNodeNS(attr);
            }

            return element;
        }
    }

    @Override
    public void setFromJson(JSONObject json) {
        value = json.toString();
    }

    @Override
    public void setFromXml(Element xml) {
        value = xml.getTextContent();

        for (int i = 0; i < xml.getAttributes().getLength(); i++) {
            Attr attr = (Attr) xml.getAttributes().item(i);
            attributes.put(attr.getNodeName(), attr.getNodeValue());
        }
    }

    @Override
    public String toString() {
        return value != null ? value : "";
    }

}