package opentraceability.models.events.kdes;

import opentraceability.Constants;
import opentraceability.interfaces.IEventKDE;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import kotlin.reflect.Type;
import kotlin.reflect.full.createType;

public class EventKDEDouble extends EventKDEBase implements IEventKDE {
    public Type valueType = Double.class.createType();
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
    public Element getXml() {
        Double value = this.value;
        if (value == null) {
            return null;
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        Document document;
        try {
            document = docFactory.newDocumentBuilder().newDocument();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        Element element = document.createElementNS(this.namespace, this.name);
        element.setTextContent(value.toString());

        Element xsiTypeAttr = document.createAttributeNS(Constants.XSI_NAMESPACE, "type");
        xsiTypeAttr.setAttributeNS(Constants.XSI_NAMESPACE, "value", "number");
        element.setAttributeNodeNS(xsiTypeAttr);

        return element;
    }

    @Override
    public void setFromJson(JSONObject json) {
        this.value = json.getDouble("value");
    }

    @Override
    public void setFromXml(Element xml) {
        try {
            this.value = Double.parseDouble(xml.getTextContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return value == null ? "" : value.toString();
    }
}