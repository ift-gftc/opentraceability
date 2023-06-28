package opentraceability.models.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.codec.language.bm.Lang;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class LanguageString {
    @JsonProperty("@language")
    public String language = "en-US";

    @JsonProperty("@value")
    public String value = "";

    public LanguageString() {

    }

    public LanguageString(JSONObject i) {
        if (i.has("@language"))
        {
            this.language = i.getString("@language");
        }
        if (i.has("@value"))
        {
            this.value = i.getString("@value");
        }
    }

    public JSONObject toJSON()
    {
        JSONObject j = new JSONObject();
        j.put("@language", this.language);
        j.put("@value", this.value);
        return j;
    }

    public static ArrayList<LanguageString> fromJSON(Object json)
    {
        ArrayList<LanguageString> languageStrings = new ArrayList<>();
        if (json instanceof String)
        {
            LanguageString l = new LanguageString();
            l.value = json.toString();
            languageStrings.add(l);
        }
        else if (json instanceof JSONObject)
        {
            LanguageString l = new LanguageString((JSONObject)json);
            languageStrings.add(l);
        }
        else if (json instanceof JSONArray)
        {
            JSONArray jArr = (JSONArray) json;
            for (var i: jArr)
            {
                if (i instanceof String)
                {
                    LanguageString l = new LanguageString();
                    l.value = i.toString();
                    languageStrings.add(l);
                }
                else if (i instanceof JSONObject)
                {
                    LanguageString l = new LanguageString((JSONObject)i);
                    languageStrings.add(l);
                }
            }
        }
        return languageStrings;
    }
}