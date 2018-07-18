package io.thecontext.ci.artifact

import java.io.File

/**
 * A [DeployableArtifact] is a generated artifact (i.e. RSS Feed and show notes) that can be "deployed" with an
 * [io.thecontext.ci.deployment.DeploymentJob]
 */
sealed class DeployableArtifact {

    /**
     * The content of a folder is the "Artifact"
     */
    data class FolderArtifact(val folder: File) : DeployableArtifact()
}


sealed class ArtifcatGenerationResult {

    data class Success(val artifact: DeployableArtifact) : ArtifcatGenerationResult()

    data class Failed(val errors: List<ArtifactGenerationError>) : ArtifcatGenerationResult()
}


data class ArtifactGenerationError(val message: String, val error: Throwable)