package opentraceability.models.events.kdes;

import opentraceability.Constants;
import opentraceability.interfaces.IEventKDE;


import opentraceability.utility.XElement;
import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;

public class EventKDEDouble extends IEventKDE {
    public Class valueType = Double.class;
    public Double value = null;

    public EventKDEDouble(){}

    public EventKDEDouble(String ns, String name) {
        this.namespace = ns;
        this.name = name;
    }

    @Override
    public Object getJson() {
        if (this.value == null) {
            return null;
        } else {
            JSONObject json = new JSONObject();
            json.put("keyName", this.value);
            return json;
        }
    }

    @Override
    public XElement getXml() throws Exception {
        Double value = this.value;
        if (value == null) {
            return null;
        }

        XElement element = new XElement(this.namespace, this.name);
        element.setValue(value.toString());
        element.SetAttributeValue(Constants.XSI_NAMESPACE, "type", "number");

        return element;
    }

    public void setFromJson(Object json) {
        if (json instanceof JSONObject)
        {
            this.value = ((JSONObject)json).getDouble("value");
        }
        else if (json instanceof Double)
        {
            this.value = (Double)json;
        }
        else if (json instanceof String)
        {
            this.value = Double.parseDouble((String)json);
        }
    }

    public void setFromXml(XElement xml) {
        this.value = Double.parseDouble(xml.getValue());
    }

    public String toString() {
        return value == null ? "" : value.toString();
    }
}