package opentraceability.models.events.kdes;

import opentraceability.interfaces.IEventKDE;
import opentraceability.utility.XAttribute;
import opentraceability.utility.XElement;
import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

import java.util.HashMap;
import java.util.Map;

public class EventKDEString extends IEventKDE {
    public Class valueType = String.class;
    public String value;
    public String type;
    public Map<String, String> attributes = new HashMap<>();

    public EventKDEString() {}

    public EventKDEString(String ns, String name) {
        this.namespace = ns;
        this.name = name;
        attributes = new HashMap<>();
    }

    public Object getJson() {
        if (value == null || value.isEmpty()) {
            return null;
        } else {
            JSONObject json = new JSONObject();
            json.put("keyName", value);
            return json;
        }
    }

    public XElement getXml() throws Exception {
        if (value == null || value.isEmpty()) {
            return null;
        } else {
            XElement element = new XElement(namespace, name);
            element.setValue(value);

            for (Map.Entry<String, String> entry : attributes.entrySet())
            {
                element.SetAttributeValue(null, entry.getKey(), entry.getValue());
            }

            return element;
        }
    }

    public void setFromJson(Object json) {
        attributes = new HashMap<>();
        value = json.toString();
    }

    public void setFromXml(XElement xml) throws Exception {
        value = xml.getValue();
        namespace = xml.getNamespaceUri();
        name = xml.getTagName();

        attributes = new HashMap<>();
        for (XAttribute xatt: xml.Attributes()) {
            attributes.put(xatt.Name, xatt.Value);
        }
    }

    @Override
    public String toString() {
        return value != null ? value : "";
    }

}