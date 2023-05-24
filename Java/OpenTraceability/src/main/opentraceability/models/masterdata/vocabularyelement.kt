package models.masterdata

import com.fasterxml.jackson.core.JsonToken
import interfaces.IMasterDataKDE
import interfaces.VocabularyType
import java.util.*
import models.identifiers.*
import java.lang.reflect.Type

class VocabularyElement /*: IVocabularyElement*/ {
    var ID: String? = null
    var EPCISType: String? = null
    var JsonLDType: String? = null
    var Context: JsonToken? = null

    /*
    var VocabularyType: VocabularyType = VocabularyType()
    public VocabularyType VocabularyType
    {
        get
        {
            VocabularyType type = VocabularyType.Unknown;
            foreach (VocabularyType t in Enum.GetValues(typeof(VocabularyType)))
            {
                if (EnumUtil.GetEnumDescription(t).Trim().ToLower() == EPCISType?.Trim().ToLower())
                {
                    type = t;
                }
            }
            return type;
        }
    }
     */


    var KDEs: List<IMasterDataKDE> = ArrayList<IMasterDataKDE>()

}
