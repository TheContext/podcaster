package io.thecontext.ci.model

import java.time.LocalDateTime

/**
 * This class is generated in the pipeline from a [Episode] if and only iff all fields are valid
 */
data class ProcessedEpisode(
        val title: String,
        val releaseDate: LocalDateTime,
        val additionalLinks: List<Link>,
        val guests: List<Person>,
        val hosts: List<Person>
)