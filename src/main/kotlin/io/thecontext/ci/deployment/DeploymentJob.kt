package io.thecontext.ci.deployment

import io.reactivex.Single

/**
 * The responsiblility of a Deployment Job is basically to publish the generated Artifacts "somewhere"
 */
interface DeploymentJob {

    /**
     * Deployment Result representing the result state of a Result
     */
    sealed class Result {
        /**
         * Deployment successful
         */
        object Success : Result()

        /**
         * Deployment failed with an error
         */
        data class Failed(val error: Throwable) : Result()
    }


    /**
     * Depoloy the stuff somewhere.
     * @return [Single] of type [Result] that indicates whether or not the depolyment succeeded or failed.
     */
    fun deploy(): Single<Result>
}