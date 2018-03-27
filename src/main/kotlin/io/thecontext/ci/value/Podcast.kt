package io.thecontext.ci.value

import com.fasterxml.jackson.annotation.JsonProperty

data class Podcast(

        @JsonProperty("title")
        val title: String,

        @JsonProperty("subtitle")
        val subtitle: String,

        @JsonProperty("summary")
        val summary: String,

        @JsonProperty("peopleIds")
        val peopleIds: PeopleIds,

        val people: People = People(),

        @JsonProperty("language")
        val language: Language,

        @JsonProperty("explicit")
        val explicit: Boolean,

        @JsonProperty("category")
        val category: String,

        @JsonProperty("subcategory")
        val subcategory: String,

        @JsonProperty("keywords")
        val keywords: List<String>,

        @JsonProperty("url")
        val url: String,

        @JsonProperty("artworkUrl")
        val artworkUrl: String,

        @JsonProperty("feedUrl")
        val feedUrl: String

) {

    data class Language(

            @JsonProperty("code")
            val code: String,

            @JsonProperty("regionCode")
            val regionCode: String

    )

    data class PeopleIds(

            @JsonProperty("owners")
            val owners: List<String>,

            @JsonProperty("authors")
            val authors: List<String>

    )

    data class People(
            val owners: List<Person> = emptyList(),
            val authors: List<Person> = emptyList()
    )

}