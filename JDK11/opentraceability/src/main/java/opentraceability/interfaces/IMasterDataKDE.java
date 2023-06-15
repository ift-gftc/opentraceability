package opentraceability.interfaces;

import org.json.JSONObject;
import org.w3c.dom.Element;

import java.lang.reflect.Type;

public abstract class IMasterDataKDE {
    public String namespace;
    public String name;
    public Type valueType;

    public abstract void setFromGS1WebVocabJson(JSONObject json);
    public abstract JSONObject getGS1WebVocabJson();
    public abstract void setFromEPCISXml(Element xml);
    public abstract Element getEPCISXml() throws Exception;
}