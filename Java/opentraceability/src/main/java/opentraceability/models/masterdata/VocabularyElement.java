package opentraceability.models.masterdata;

import java.util.ArrayList;
import java.util.List;
import opentraceability.interfaces.IMasterDataKDE;
import opentraceability.interfaces.IVocabularyElement;
import opentraceability.interfaces.VocabularyType;
import opentraceability.utility.attributes.OpenTraceabilityExtensionElementsAttribute;

public class VocabularyElement extends IVocabularyElement {
    String id;

    public VocabularyElement()
    {
        epcisType = "urn:epcglobal:epcis:vtype:Unknown";
        vocabularyType = VocabularyType.Unknown;
    }

    @OpenTraceabilityExtensionElementsAttribute
    public List<IMasterDataKDE> kdes = new ArrayList<>();

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String val) {
        id = val;
    }
}