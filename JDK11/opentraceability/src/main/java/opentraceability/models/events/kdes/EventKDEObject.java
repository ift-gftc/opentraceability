package opentraceability.models.events.kdes;

import opentraceability.Constants;
import opentraceability.interfaces.IEventKDE;
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
    public Element _xml = null;
    public JSONObject _json = null;

    public EventKDEObject() {}

    public EventKDEObject(String ns, String name) {
        this.namespace = ns;
        this.name = name;
    }


    public JSONObject getJson() {
        if (_xml != null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document xmlDoc = builder.parse(new InputSource(new StringReader(_xml.toString())));

                JSONObject json = XML.toJSONObject(xmlDoc.toString());
                return new JSONObject(json.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else if (_json != null) {
            return _json;
        } else {
            return null;
        }
    }


    public Element getXml() {
        if (_xml != null) {
            return _xml;
        } else if (_json != null) {
            try {
                JSONObject xmlDoc = XML.toJSONObject(_json.toString());
                String xmlStr = xmlDoc.toString();
                if (!xmlStr.isEmpty()) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(new InputSource(new StringReader(xmlStr)));
                    return document.getDocumentElement();
                }

                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public void setFromJson(JSONObject json) {
        _xml = null;
        _json = json;
    }

    public void setFromXml(Element xml) {
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