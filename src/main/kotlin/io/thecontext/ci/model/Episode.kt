package io.thecontext.ci.model

import java.time.LocalDateTime

/**
 * Created by hannes on 13.05.17.
 */
data class Episode(
        val title: String,
        val showNotes: String,
        val releaseDate: LocalDateTime,
        val additionalLinks: List<Link>,
        val guests: List<String>,
        val hosts : List<String>
)


fun Episode.showNotesFileName() = title.replace(" ", "_").replace(":", "_").replace("@", "at")+".md"