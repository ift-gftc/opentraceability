package opentraceability.models.events.kdes;

import opentraceability.interfaces.IEventKDE;
import opentraceability.utility.Countries;
import opentraceability.utility.Country;
import opentraceability.utility.XElement;
import org.json.JSONObject;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;


public class EventKDECountry extends IEventKDE {
    public Class valueType = Country.class;

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
    public XElement getXml() {
        Country value = this.value;
        if (value == null) return null;
        try {
            var element = new XElement(this.name);
            element.setValue(Integer.toString(value.iso));
            return element;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void setFromJson(Object json) {
        String strValue = json.toString();
        this.value = Countries.parse(strValue);
    }

    @Override
    public void setFromXml(XElement xml) {
        String strValue = xml.getValue();
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