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

fun List<Person>.find(id: String) = this.find { it.id == id } ?: throw IllegalArgumentException("Person [$id] is not available.")
