package io.thecontext.ci.deployment

import io.reactivex.Single
import java.io.File

/**
 * Uses Git to deploy stuff.
 * This implementation assumes that git (command line) is installed locally on the machine that runs this code
 */
class GitDeploymentJob(
        /**
         * The repository that the should be checked out
         */
        val repositoryUrl: String,

        /**
         * The path to where repositoryUrl should be checked out at
         */
        val localRepoDestination: File,

        /**
         * The name of the branch that should be checked out and that should be used to deployed to
         */
        val branch: String,

        /**
         * The commit message that is used to commit stuff
         */
        val commitMessage: String
) : DeploymentJob {

    override fun deploy(): Single<DeploymentJob.Result> = Single.fromCallable {
        Runtime.getRuntime().run {
            exec("cd $localRepoDestination")
            exec("git clone $repositoryUrl")
            exec("git checkout $branch")
            exec("git add .")
            exec("git commit -am \"$commitMessage\"")
        }
        DeploymentJob.Result.Success
    }
}