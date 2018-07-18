package io.thecontext.ci.artifact.website

import io.reactivex.Single
import io.thecontext.ci.artifact.ArtifactGenerationError
import io.thecontext.ci.artifact.ArtifcatGenerationResult
import io.thecontext.ci.artifact.DeployableArtifact
import io.thecontext.ci.artifact.TextWriter
import io.thecontext.ci.utils.Either
import io.thecontext.ci.utils.right
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Podcast
import java.io.File

/**
 * Generates the Artifcats for the website
 */
interface WebsiteArtifactGenerator {

    fun write(directory: File, podcast: Podcast, episodes: List<Episode>): Single<ArtifcatGenerationResult>

    class Impl(
            private val websiteFormatter: WebsiteFormatter,
            private val textWriter: TextWriter
    ) : WebsiteArtifactGenerator {

        override fun write(directory: File, podcast: Podcast, episodes: List<Episode>): Single<ArtifcatGenerationResult> {

            val createDirs = Single.fromCallable { directory.mkdirs() }

            val episodeWebsite: List<Single<Either<Unit, ArtifactGenerationError>>> = episodes.map { episode ->
                websiteFormatter.format(
                        podcast = podcast,
                        episode = episode
                ).map<Either<Unit, ArtifactGenerationError>> { websiteContent ->
                    textWriter.write(File(directory, "${episode.date}-${episode.slug}.md"), websiteContent)
                    Either.Left(Unit)
                }.onErrorReturn { Either.Right(ArtifactGenerationError("Could not generate Front Matter for episode ${episode.title}", it)) }
            }

            return createDirs.flatMap { _ ->
                Single.zip(episodeWebsite) {generationResults ->
                    val resutls = generationResults.map { it as  Either<Unit, ArtifactGenerationError> }
                    val (_, errors) = resutls
                            .partition {
                                when (it) {
                                    is Either.Left<Unit> -> true
                                    is Either.Right<ArtifactGenerationError> -> false
                                }
                            }

                    if (errors.isEmpty()) {
                        ArtifcatGenerationResult.Success(DeployableArtifact.FolderArtifact(directory))
                    } else {
                        ArtifcatGenerationResult.Failed(errors = errors.map { it.right() })
                    }
                }
            }
        }
    }
}