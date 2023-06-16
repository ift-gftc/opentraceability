package opentraceability.models.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LanguageString {
    @JsonProperty("@language")
    public String language = "";

    @JsonProperty("@value")
    public String value = "";
}