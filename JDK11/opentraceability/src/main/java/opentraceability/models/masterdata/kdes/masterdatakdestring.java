package opentraceability.models.masterdata.kdes;

import opentraceability.interfaces.IMasterDataKDE;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MasterDataKDEString extends MasterDataKDEBase implements IMasterDataKDE {

    public Type valueType = String.class;

    public String value = null;
    public String type = null;
    public Map<String, String> attributes = new HashMap<>();

    public MasterDataKDEString() {
    }

    public MasterDataKDEString(Type valueType) {
        // Default constructor
        this.valueType = valueType;
    }

    public MasterDataKDEString(String ns, String name) {
        super.namespace = ns;
        super.name = name;
    }

    public JSONObject getJson() {
        if (value == null || value.isEmpty()) {
            return null;
        } else {
            return new JSONObject(value);
        }
    }


    public Element getXml() throws Exception {
        if (value == null || value.isEmpty()) {
            return null;
        } else {
            String xname = namespace + name;
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element element = document.createElement(xname);
            element.setTextContent(value);

            // Set the xsi type...
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();
                element.setAttribute(key, val);
            }

            return element;
        }
    }


    public void setFromJson(JSONObject json) {
        value = json.toString();
    }

    public void setFromXml(Element xml) {
        value = xml.getTextContent();

        for (int i = 0; i < xml.getAttributes().getLength(); i++) {
            String key = xml.getAttributes().item(i).getNodeName();
            String val = xml.getAttributes().item(i).getNodeValue();
            attributes.put(key, val);
        }
    }

    @Override
    public String toString() {
        return value == null ? "" : value;
    }

    @Override
    public void setFromGS1WebVocabJson(JSONObject json) {
        value = json.toString();
    }

    public JSONObject getGS1WebVocabJson() {
        if (value != null) {
            return new JSONObject(value);
        } else {
            return null;
        }
    }

    @Override
    public void setFromEPCISXml(Element xml) {
        name = xml.getAttribute("id") != null ? xml.getAttribute("id") : "";
        value = xml.getTextContent();
    }

    @Override
    public Element getEPCISXml() throws Exception {
        if (value != null) {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element element = document.createElement("attribute");
            element.setAttribute("id", name);
            element.setTextContent(value);
            return element;
        } else {
            return null;
        }
    }
}