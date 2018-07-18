package io.thecontext.ci.artifact

import io.reactivex.Single
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Podcast

/**
 * As the name already suggests, a [ArtifactGenerator] generates a [DeployableArtifact] which eventually gets deployed
 * via [io.thecontext.ci.deployment.DeploymentJob]
 */
interface ArtifactGenerator {

    /**
     * Generates the [DeployableArtifact]. Doesn't throw an error, rather it returns an [ArtifactGenerationResult]
     */
    fun generateArtifact(podcast: Podcast, episodes: List<Episode>): Single<ArtifactGenerationResult>
}


/**
 * The Result of [ArtifactGenerator]
 */
sealed class ArtifactGenerationResult {

    /**
     * The case when generating the [DeployableArtifact] succeeded
     */
    data class Success(val artifact: DeployableArtifact) : ArtifactGenerationResult()

    /**
     * The case when generating [DeployableArtifact] failed. A list of [ArtifactGenerationError] provides
     * details why the generation failed
     */
    data class Failure(val errors: List<ArtifactGenerationError>) : ArtifactGenerationResult()
}


data class ArtifactGenerationError(val message: String, val error: Throwable)