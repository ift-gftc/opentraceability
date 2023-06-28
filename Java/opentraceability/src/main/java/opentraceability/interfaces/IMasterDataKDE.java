package opentraceability.interfaces;

import opentraceability.utility.XElement;
import org.json.JSONObject;
import org.w3c.dom.Element;



public abstract class IMasterDataKDE {
    public String namespace;
    public String name;
    public Class valueType;

    public abstract void setFromGS1WebVocabJson(Object json);
    public abstract Object getGS1WebVocabJson();
    public abstract void setFromEPCISXml(XElement xml) throws Exception;
    public abstract XElement getEPCISXml() throws Exception;
}