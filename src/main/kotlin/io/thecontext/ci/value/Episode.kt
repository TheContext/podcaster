package io.thecontext.ci.value

import com.fasterxml.jackson.annotation.JsonProperty

data class Episode(

        val slug: String = "",
  
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

        @JsonProperty("peopleIds")
        val people: People,

        @JsonProperty("url")
        val url: String,

        @JsonProperty("discussionUrl")
        val discussionUrl: String,

        @JsonProperty("date")
        val date: String,

        @JsonProperty("duration")
        val duration: String,

        @JsonProperty("file")
        val file: File,

        val notesMarkdown: String = ""

) {

    data class People(

            @JsonProperty("hosts")
            val hostIds: List<String>,

            @JsonProperty("guests")
            val guestIds: List<String>

    )

    data class File(

            @JsonProperty("url")
            val url: String,

            @JsonProperty("length")
            val length: Long

    )

}