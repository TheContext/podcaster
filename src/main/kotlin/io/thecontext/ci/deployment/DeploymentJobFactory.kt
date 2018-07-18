package io.thecontext.ci.deployment

import io.reactivex.Single
import io.thecontext.ci.artifact.DeployableArtifact

fun createDeploymentJob(artifact : DeployableArtifact) : DeploymentJob =
        // TODO this is just stub and should use the real deployments once we are ready to ship everything
        object : DeploymentJob {
            override fun deploy(): Single<DeploymentJob.Result> = Single.just(DeploymentJob.Result.Success)
        }