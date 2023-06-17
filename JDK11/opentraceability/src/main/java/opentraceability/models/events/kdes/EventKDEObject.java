package opentraceability.models.events.kdes;

import opentraceability.Constants;
import opentraceability.interfaces.IEventKDE;
import opentraceability.utility.XElement;
import opentraceability.utility.XmlToJsonConverter;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.lang.reflect.Type;
import org.xml.sax.InputSource;

public class EventKDEObject extends IEventKDE {
    public Type valueType = Object.class;

    public Object value = null;
    public XElement _xml = null;
    public JSONObject _json = null;

    public EventKDEObject() {}

    public EventKDEObject(String ns, String name) {
        this.namespace = ns;
        this.name = name;
    }


    public Object getJson() {
        if (_xml != null) {
            JSONObject j = XmlToJsonConverter.toJSON(_xml);
            return j;
        } else if (_json != null) {
            return _json;
        } else {
            return null;
        }
    }


    public XElement getXml() {
        if (_xml != null) {
            return _xml;
        }
        else if (_json != null)
        {
            XElement x = XmlToJsonConverter.toXML(_json);
            return x;
        }
        else
        {
            return null;
        }
    }

    public void setFromJson(Object json) throws Exception {
        _xml = null;
        if (json instanceof JSONObject)
        {
            _json = (JSONObject)json;
        }
        else throw new Exception("_json is not JSONObject");
    }

    public void setFromXml(XElement xml) {
        _xml = xml;
        _json = null;
    }

    public String toString() {
        if (value != null) {
            return value.toString();
        } else {
            return "";
        }
    }
}