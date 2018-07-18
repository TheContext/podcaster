package io.thecontext.ci.artifact.website

import io.reactivex.Single
import io.thecontext.ci.artifact.ArtifactGenerationError
import io.thecontext.ci.artifact.ArtifactGenerationResult
import io.thecontext.ci.artifact.ArtifactGenerator
import io.thecontext.ci.artifact.DeployableArtifact
import io.thecontext.ci.artifact.TextWriter
import io.thecontext.ci.utils.Either
import io.thecontext.ci.utils.Singles
import io.thecontext.ci.utils.right
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Podcast
import java.io.File

/**
 * Generates the artifacts for the website
 */
class WebsiteArtifactGenerator(
        private val websiteFormatter: WebsiteFormatter,
        private val textWriter: TextWriter,
        private val directoryForSavingWebsite: File
) : ArtifactGenerator {

    override fun generateArtifact(podcast: Podcast, episodes: List<Episode>): Single<ArtifactGenerationResult> {
        val createDirs = Single.fromCallable { directoryForSavingWebsite.mkdirs() }

        val episodeWebsite: List<Single<Either<Unit, ArtifactGenerationError>>> = episodes.map { episode ->
            websiteFormatter.format(
                    podcast = podcast,
                    episode = episode
            ).map<Either<Unit, ArtifactGenerationError>> { websiteContent ->
                textWriter.write(File(directoryForSavingWebsite, "${episode.date}-${episode.slug}.md"), websiteContent)
                Either.Left(Unit)
            }.onErrorReturn { Either.Right(ArtifactGenerationError("Could not generate Front Matter for episode ${episode.title}. Error $it", it)) }
        }

        return createDirs.flatMap { _ ->
            Singles.zip(episodeWebsite) { results ->
                val (_, errors) = results
                        .partition {
                            when (it) {
                                is Either.Left<Unit> -> true
                                is Either.Right<ArtifactGenerationError> -> false
                            }
                        }

                if (errors.isEmpty()) {
                    ArtifactGenerationResult.Success(DeployableArtifact.FolderArtifact(directoryForSavingWebsite))
                } else {
                    ArtifactGenerationResult.Failure(errors = errors.map { it.right() })#
                }
            }
        }
    }
}
