package opentraceability.models.masterdata;

import java.util.List;
import opentraceability.interfaces.IMasterDataKDE;
import opentraceability.interfaces.IVocabularyElement;
import opentraceability.interfaces.VocabularyType;
import opentraceability.models.identifiers.*;
import opentraceability.utility.EnumUtil;
import org.json.JSONObject;

public class VocabularyElement implements IVocabularyElement {
    public String id = null;
    public String epcisType = "";
    public String jsonLDType = null;
    public JSONObject context = null;

    public VocabularyType vocabularyType = VocabularyType.Unknown;

    public List<IMasterDataKDE> kdes = new List<IMasterDataKDE>();

    public VocabularyElement() {
    }

    public VocabularyType getVocabularyType() {
        VocabularyType type = VocabularyType.Unknown;
        for (VocabularyType t : VocabularyType.values()) {
            if (EnumUtil.GetEnumDescription(t).trim().equals(epcisType.trim())) {
                type = t;
            }
        }
        return type;
    }

    public void setVocabularyType(VocabularyType value) {
        vocabularyType = value;
    }
}