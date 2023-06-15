package opentraceability.models.masterdata.kdes;

import opentraceability.interfaces.IMasterDataKDE;
import opentraceability.utility.XmlToJsonConverter;
import org.json.JSONObject;
import org.w3c.dom.Element;
import java.lang.reflect.Type;

public class MasterDataKDEObject extends IMasterDataKDE  {
    private Element _xml = null;
    private JSONObject _json = null;

    Type valueType = Object.class;

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

    public void setFromGS1WebVocabJson(JSONObject json) {
        _xml = null;
        _json = json;
    }

    public JSONObject getGS1WebVocabJson() {
        if (_xml != null) {
            JSONObject json = XmlToJsonConverter.toJSON(_xml);
            return json;
        } else {
            return _json;
        }
    }

    public void setFromEPCISXml(Element xml) {
        _xml = xml;
        _json = null;
    }

    public Element getEPCISXml() {
        Element xml;
        if (_xml != null) {
            xml = _xml;
        } else if (_json != null) {
            xml = XmlToJsonConverter.toXML(_json);
        } else {
            xml = null;
        }
        
        return xml;
    }
}