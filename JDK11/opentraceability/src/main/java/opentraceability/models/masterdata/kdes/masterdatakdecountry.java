package opentraceability.models.masterdata.kdes;

import opentraceability.interfaces.IMasterDataKDE;
import opentraceability.utility.Countries;
import opentraceability.utility.Country;
import org.json.JSONObject;
import org.w3c.dom.Element;

import java.lang.reflect.Type;

import static opentraceability.utility.OTXmlUtil.*;

public class MasterDataKDECountry extends MasterDataKDEBase implements IMasterDataKDE {
    public Country value = null;

    public Type getValueType() {
        return Country.class;
    };

    public Element getEPCISXml() throws Exception {
        if (value == null) {
            return null;
        } else {
            Element x = createXmlElement("attribute");
            x.setAttribute("id", name);
            x.setTextContent(value.alpha3);
            return x;
        }
    }

    public JSONObject getGS1WebVocabJson() {
        throw new UnsupportedOperationException("Not Implemented Exception");
    }

    public void setFromEPCISXml(Element xml) {
        Country country = Countries.parse(xml.getNodeValue());
        value = country;
        name = xml.getAttribute("id") != null ? xml.getAttribute("id") : "";
    }

    public void setFromGS1WebVocabJson(JSONObject json) {
        throw new UnsupportedOperationException("Not Implemented Exception");
    }

    @Override
    public String toString() {
        return value != null ? value.name : "";
    }
}