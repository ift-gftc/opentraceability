package opentraceability.interfaces;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class IVocabularyElement {
    public String id = null;

    public JSONObject context = null;

    public String epcisType = null;

    public VocabularyType vocabularyType = null;

    public ArrayList<IMasterDataKDE> kdes = new ArrayList<IMasterDataKDE>();
}