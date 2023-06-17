package opentraceability.models.events.kdes;

import opentraceability.Constants;
import opentraceability.interfaces.IEventKDE;
import opentraceability.utility.XElement;
import org.json.JSONObject;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.reflect.Type;

public class EventKDEBoolean extends IEventKDE {

    public Type valueType = Boolean.class;

    public Boolean value;

    public EventKDEBoolean() {
    }

    public EventKDEBoolean(String ns, String name) {
        this.namespace = ns;
        this.name = name;
    }

    public Object getJson() {
        if (value != null) {
            return new JSONObject().put("value", value);
        } else {
            return null;
        }
    }

    public XElement getXml() throws Exception {
        if (value != null) {
            var element = new XElement(namespace, name);
            element.SetAttributeValue(Constants.XSI_NAMESPACE, "type", "boolean");
            element.setValue(value.toString());
            return element;
        } else {
            return null;
        }
    }

    public void setFromJson(Object json) {
        if (json instanceof JSONObject)
        {
            if (((JSONObject)json).has("value")) {
                this.value = ((JSONObject)json).getBoolean("value");
            }
        }
        else if (json instanceof Boolean)
        {
            this.value = (Boolean) json;
        }
        else if (json instanceof String)
        {
            this.value = Boolean.parseBoolean((String)json);
        }
    }

    public void setFromXml(XElement xml) {
        if (xml != null) {
            this.value = Boolean.parseBoolean(xml.getValue());
        }
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "";
    }
}