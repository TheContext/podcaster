package io.thecontext.ci.value

import com.fasterxml.jackson.annotation.JsonProperty

data class Person(

        @JsonProperty("id")
        val id: String,

        @JsonProperty("name")
        val name: String,

        @JsonProperty("twitter")
        val twitter: String?,

        @JsonProperty("github")
        val github: String?,

        @JsonProperty("website")
        val site: String?

)

