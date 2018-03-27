package io.thecontext.ci.value

import com.fasterxml.jackson.annotation.JsonProperty

data class Episode(

        val slug: String = "",

        @JsonProperty("number")
        val number: Int,

        @JsonProperty("title")
        val title: String,

        @JsonProperty("peopleIds")
        val peopleIds: PeopleIds,

        val people: People = People(),

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

        @JsonProperty("notes")
        val notes: Notes

) {

    data class PeopleIds(

            @JsonProperty("hosts")
            val hosts: List<String>,

            @JsonProperty("guests")
            val guests: List<String>

    )

    data class People(
            val hosts: List<Person> = emptyList(),
            val guests: List<Person> = emptyList()
    )

    data class File(

            @JsonProperty("url")
            val url: String,

            @JsonProperty("length")
            val length: Long
    )

    data class Notes(

            @JsonProperty("links")
            val links: List<Link>,

            val descriptionMarkdown: String = ""

    )

}