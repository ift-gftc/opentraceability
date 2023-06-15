package opentraceability.models.events.kdes;

import opentraceability.interfaces.IEventKDE;
import opentraceability.utility.Countries;
import opentraceability.utility.Country;
import org.json.JSONObject;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Type;

public class EventKDECountry implements IEventKDE {
    public Type valueType = Country.class;

    public Country value = null;

    public EventKDECountry() {
    }

    public EventKDECountry(String ns, String name) {
        this.namespace = ns;
        this.name = name;
    }

    @Override
    public JSONObject getJson() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Element getXml() {
        Country value = this.value;
        if (value == null) return null;
        try {
            // you would typically use a XML parser here
            var documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            var document = documentBuilder.newDocument();
            var element = document.createElement(this.name);
            element.setTextContent(value.iso.toString());
            return element;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void setFromJson(JSONObject json) {
        String strValue = json.toString();
        this.value = Countries.parse(strValue);
    }

    @Override
    public void setFromXml(Element xml) {
        String strValue = xml.getTextContent();
        this.value = Countries.parse(strValue);
    }

    @Override
    public String toString() {
        if (value != null) {
            return value.toString();
        } else {
            return "";
        }
    }
}