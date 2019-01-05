package io.thecontext.ci.input

import io.reactivex.Scheduler
import io.reactivex.Single
import java.io.File

interface InputFilesLocator {

    sealed class Result {
        data class Success(val people: File, val podcast: File, val episodes: Map<File, File>) : Result()
        data class Failure(val message: String) : Result()
    }

    object FileNames {
        const val PEOPLE = "people.yaml"
        const val PODCAST = "podcast.yaml"
        const val EPISODE = "episode.yaml"
        const val EPISODE_NOTES = "notes.md"
    }

    fun locate(directory: File): Single<Result>

    class Impl(
            private val ioScheduler: Scheduler
    ) : InputFilesLocator {

        override fun locate(directory: File) = Single
                .fromCallable {
                    if (!directory.exists()) {
                        return@fromCallable Result.Failure("Podcast directory does not exist.")
                    }

                    if (!directory.isDirectory) {
                        return@fromCallable Result.Failure("Podcast directory [${directory.path}] is not actually a directory.")
                    }

                    val people = File(directory, FileNames.PEOPLE)

                    if (!people.exists()) {
                        return@fromCallable Result.Failure("Podcast directory does not contain [${FileNames.PEOPLE}].")
                    }

                    val podcast = File(directory, FileNames.PODCAST)

                    if (!podcast.exists()) {
                        return@fromCallable Result.Failure("Podcast directory does not contain [${FileNames.PODCAST}].")
                    }

                    val episodes = directory
                            .walk()
                            .maxDepth(1)
                            .filter { it.isDirectory && !it.isHidden && it != directory }
                            .map { File(it, FileNames.EPISODE) to File(it, FileNames.EPISODE_NOTES) }
                            .toMap()

                    episodes.forEach { (episode, episodeDescription) ->
                        if (!episode.exists()) {
                            return@fromCallable Result.Failure("Episode directory [${episode.parentFile.path}] does not contain [${FileNames.EPISODE}].")
                        }

                        if (!episodeDescription.exists()) {
                            return@fromCallable Result.Failure("Episode directory [${episodeDescription.parentFile.path}] does not contain [${FileNames.EPISODE_NOTES}].")
                        }
                    }

                    Result.Success(people, podcast, episodes)
                }
                .subscribeOn(ioScheduler)
    }
}