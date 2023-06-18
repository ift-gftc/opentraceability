package opentraceability.models.masterdata.kdes;

import opentraceability.interfaces.IMasterDataKDE;
import opentraceability.utility.Countries;
import opentraceability.utility.Country;
import opentraceability.utility.XElement;
import org.json.JSONObject;




public class MasterDataKDECountry extends IMasterDataKDE {
    public Country value = null;

    public Class getValueType() {
        return Country.class;
    }

    public XElement getEPCISXml() throws Exception {
        if (value == null) {
            return null;
        } else {
            XElement x = new XElement("attribute");
            x.SetAttributeValue("id", this.name);
            x.setValue(value.alpha3);
            return x;
        }
    }

    public JSONObject getGS1WebVocabJson() {
        throw new UnsupportedOperationException("Not Implemented Exception");
    }

    public void setFromEPCISXml(XElement xml) throws Exception {
        Country country = Countries.parse(xml.getValue());
        value = country;
        name = xml.Attribute("id");
    }

    public void setFromGS1WebVocabJson(JSONObject json) {
        throw new UnsupportedOperationException("Not Implemented Exception");
    }

    @Override
    public String toString() {
        return value != null ? value.name : "";
    }
}