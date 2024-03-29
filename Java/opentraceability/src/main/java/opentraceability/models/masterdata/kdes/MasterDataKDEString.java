package opentraceability.models.masterdata.kdes;

import opentraceability.interfaces.IMasterDataKDE;
import opentraceability.utility.XElement;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

import java.util.HashMap;
import java.util.Map;

public class MasterDataKDEString extends IMasterDataKDE {

    public Class valueType = String.class;

    public String value = null;
    public String type = null;
    public Map<String, String> attributes = new HashMap<>();

    public MasterDataKDEString() {
    }

    public MasterDataKDEString(Class valueType) {
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
    public void setFromGS1WebVocabJson(Object json) {
        value = json.toString();
    }

    public Object getGS1WebVocabJson() {
        if (value != null) {
            return value;
        } else {
            return null;
        }
    }

    @Override
    public void setFromEPCISXml(XElement xml) throws Exception {
        name = xml.Attribute("id");
        value = xml.getValue();
    }

    @Override
    public XElement getEPCISXml() throws Exception {
        if (value != null) {
            XElement element = new XElement("attribute");
            element.SetAttributeValue("id", name);
            element.setValue(value);
            return element;
        } else {
            return null;
        }
    }
}