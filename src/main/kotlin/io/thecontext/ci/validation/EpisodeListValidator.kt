package io.thecontext.ci.validation

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.value.Episode

class EpisodeListValidator(
        private val ioScheduler: Scheduler
) : Validator<List<Episode>> {

    private val Episode.readableIdForError
        get() = title

    override fun validate(value: List<Episode>): Single<ValidationResult> = Single.fromCallable {
        val uniqueIds = HashMap<String, Episode>()
        val errorMessage = StringBuilder()
        value.forEach { episode ->
            val episodeWithSameId: Episode? = uniqueIds.putIfAbsent(episode.id, episode)
            if (episodeWithSameId != null) {
                if (errorMessage.isNotEmpty())
                    errorMessage.appendln()

                errorMessage.append("Error: Episode id must be unique but id = \"")
                        .append(episode.id)
                        .append("\" is used for \"")
                        .append(episode.readableIdForError)
                        .append("\" and \"")
                        .append(episodeWithSameId.readableIdForError)
                        .append('\"')
            }
        }

        if (errorMessage.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(errorMessage.toString())
        }
    }.subscribeOn(ioScheduler)
}