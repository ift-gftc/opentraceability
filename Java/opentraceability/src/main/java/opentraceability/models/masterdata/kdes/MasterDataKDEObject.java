package opentraceability.models.masterdata.kdes;

import opentraceability.interfaces.IMasterDataKDE;
import opentraceability.utility.XElement;
import opentraceability.utility.XmlToJsonConverter;
import org.json.JSONObject;
import org.w3c.dom.Element;


public class MasterDataKDEObject extends IMasterDataKDE  {
    private Element _xml = null;
    private Object _json = null;
    Class valueType = Object.class;

    public Object getValue() {
        return _xml != null ? _xml : _json;
    }

    public void setValue(Object value) {
        if (value instanceof Element) {
            _xml = (Element) value;
            _json = null;
        } else if (value instanceof JSONObject) {
            _xml = null;
            _json = (JSONObject) value;
        } else {
            throw new IllegalArgumentException("Unsupported value type: " + value.getClass());
        }
    }

    public MasterDataKDEObject() {}

    public MasterDataKDEObject(String ns, String name) {
        super.namespace = ns;
        super.name = name;
    }

    public void setFromGS1WebVocabJson(Object json) {
        _xml = null;
        _json = json;
    }

    public Object getGS1WebVocabJson() {
        if (_xml != null) {
            JSONObject json = XmlToJsonConverter.toJSON(new XElement(_xml));
            return json;
        } else {
            return _json;
        }
    }

    public void setFromEPCISXml(XElement xml) {
        _xml = xml.element;
        _json = null;
    }

    public XElement getEPCISXml() {
        Element xml;
        if (_xml != null) {
            xml = _xml;
        } else if (_json != null) {
            xml = XmlToJsonConverter.toXML((JSONObject)_json).element;
        } else {
            xml = null;
        }
        
        return new XElement(xml);
    }
}