package opentraceability.interfaces;

import opentraceability.utility.XElement;
import org.json.JSONObject;
import org.w3c.dom.Element;

import java.lang.reflect.Type;

public abstract class IMasterDataKDE {
    public String namespace;
    public String name;
    public Type valueType;

    public abstract void setFromGS1WebVocabJson(JSONObject json);
    public abstract JSONObject getGS1WebVocabJson();
    public abstract void setFromEPCISXml(XElement xml);
    public abstract XElement getEPCISXml() throws Exception;
}