package io.thecontext.ci.value

import com.fasterxml.jackson.annotation.JsonProperty

data class Link(

        @JsonProperty("title")
        val title: String,

        @JsonProperty("url")
        val url: String

)