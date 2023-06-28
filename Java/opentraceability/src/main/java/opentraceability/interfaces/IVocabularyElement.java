package opentraceability.interfaces;

import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class IVocabularyElement {
    public abstract String getId();
    public abstract void setId(String val);

    public JSONObject context = null;

    public String epcisType = null;

    public VocabularyType vocabularyType = null;

    @OpenTraceabilityJsonAttribute(name = "@type")
    public String type;

    public ArrayList<IMasterDataKDE> kdes = new ArrayList<IMasterDataKDE>();

    public String getEpcisType()
    {
        return epcisType;
    }
}