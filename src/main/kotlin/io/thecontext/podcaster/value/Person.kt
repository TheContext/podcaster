package io.thecontext.podcaster.value

import com.fasterxml.jackson.annotation.JsonProperty

data class Person(

        @JsonProperty("id")
        val id: String,

        @JsonProperty("name")
        val name: String,

        @JsonProperty("email")
        val email: String?,

        @JsonProperty("twitter")
        val twitter: String?,

        @JsonProperty("github")
        val github: String?,

        @JsonProperty("links")
        val links: List<Link>?

) {

    data class Link(

            @JsonProperty("name")
            val name: String,

            @JsonProperty("url")
            val url: String

    )

}

fun List<Person>.find(id: String) = this.find { it.id == id } ?: throw IllegalArgumentException("Person [$id] is not available.")
