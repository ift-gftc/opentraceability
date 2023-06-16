package opentraceability.models.masterdata;

import java.util.ArrayList;
import java.util.List;
import opentraceability.interfaces.IMasterDataKDE;
import opentraceability.interfaces.IVocabularyElement;
import opentraceability.interfaces.VocabularyType;
import opentraceability.models.identifiers.*;
import opentraceability.utility.EnumUtil;
import org.json.JSONObject;

public class VocabularyElement extends IVocabularyElement {
    public VocabularyElement()
    {
        epcisType = "urn:epcglobal:epcis:vtype:Unknown";
        vocabularyType = VocabularyType.Unknown;
    }
}