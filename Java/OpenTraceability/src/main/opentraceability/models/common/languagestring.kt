package models.common

import com.fasterxml.jackson.annotation.JsonProperty

data class LanguageString(
    @JsonProperty("@language") var language: String = "",
    @JsonProperty("@value") var value: String = ""
)