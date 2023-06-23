package opentraceability.models.common

import com.fasterxml.jackson.annotation.JsonProperty

class LanguageString(
    @JsonProperty("@language") var language: String = "",
    @JsonProperty("@value") var value: String = ""
)