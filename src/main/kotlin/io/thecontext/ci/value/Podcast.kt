package io.thecontext.ci.value

import com.fasterxml.jackson.annotation.JsonProperty

data class Podcast(

        @JsonProperty("title")
        val title: String,

        @JsonProperty("description")
        val description: String,

        @JsonProperty("people")
        val people: People,

        @JsonProperty("language")
        val language: String,

        @JsonProperty("explicit")
        val explicit: Boolean,

        @JsonProperty("category")
        val category: String,

        @JsonProperty("subcategory")
        val subcategory: String,

        @JsonProperty("url")
        val url: String,

        @JsonProperty("artworkUrl")
        val artworkUrl: String

) {

    data class People(

            @JsonProperty("owner")
            val ownerId: String,

            @JsonProperty("authors")
            val authorIds: List<String>

    )

}