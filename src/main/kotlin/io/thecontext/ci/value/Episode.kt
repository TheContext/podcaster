package io.thecontext.ci.value

import com.fasterxml.jackson.annotation.JsonProperty

data class Episode(

        @JsonProperty("id")
        val id: String,

        @JsonProperty("number")
        val number: Int,

        @JsonProperty("part")
        val part: Int?,
  
        @JsonProperty("title")
        val title: String,

        @JsonProperty("description")
        val description: String,

        @JsonProperty("people")
        val people: People,

        @JsonProperty("discussionUrl")
        val discussionUrl: String,

        @JsonProperty("time")
        val time: String,

        @JsonProperty("duration")
        val duration: String,

        @JsonProperty("file")
        val file: File,

        val slug: String = "",

        val notesMarkdown: String = ""

) {

    data class People(

            @JsonProperty("hosts")
            val hostIds: List<String>,

            @JsonProperty("guests")
            val guestIds: List<String>?

    )

    data class File(

            @JsonProperty("url")
            val url: String,

            @JsonProperty("length")
            val length: Long

    )

}