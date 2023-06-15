package opentraceability.models.events.kdes;

import opentraceability.Constants;
import opentraceability.interfaces.IEventKDE;
import org.json.JSONObject;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.reflect.Type;

public class EventKDEBoolean implements IEventKDE {

    public Type valueType = Boolean.class;

    public Boolean value;

    public EventKDEBoolean() {
    }

    public EventKDEBoolean(String ns, String name) {
        this.namespace = ns;
        this.name = name;
    }

    @Override
    public Object getJson() {
        if (value != null) {
            return new JSONObject().put("value", value);
        } else {
            return null;
        }
    }

    @Override
    public Element getXml() throws ParserConfigurationException {
        if (value != null) {
            var documentBuilderFactory = DocumentBuilderFactory.newInstance();
            var documentBuilder = documentBuilderFactory.newDocumentBuilder();
            var document = documentBuilder.newDocument();
            var element = document.createElementNS(namespace, name);
            element.setAttributeNS(Constants.XSI_NAMESPACE, "type", "boolean");
            element.setTextContent(value.toString());
            return element;
        } else {
            return null;
        }
    }

    @Override
    public void setFromJson(JSONObject json) {
        if (json.has("value")) {
            this.value = json.getBoolean("value");
        }
    }

    @Override
    public void setFromXml(Element xml) {
        if (xml != null) {
            this.value = Boolean.parseBoolean(xml.getTextContent());
        }
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "";
    }
}