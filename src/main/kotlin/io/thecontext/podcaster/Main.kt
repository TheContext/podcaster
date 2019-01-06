package io.thecontext.podcaster

import io.thecontext.podcaster.context.Context
import java.io.File
import kotlin.system.exitProcess

enum class ResultCode(val value: Int) {
    Success(0),
    Failure(1),
}
fun main(args: Array<String>) {
    val argumentsResult = Arguments.from(args)

    val resultCode = when (argumentsResult) {
        is ArgumentsResult.Success -> {
            val arguments = argumentsResult.arguments

            val inputDirectory = File(arguments.inputPath)
            val outputFeedFile = File(arguments.outputFeedPath)
            val outputWebsiteDirectory = File(arguments.outputWebsitePath)

            val runner = Runner.Impl(Context.Impl())

            val result = runner.run(inputDirectory, outputFeedFile, outputWebsiteDirectory).blockingGet()

            when (result) {
                is Runner.Result.Success -> {
                    println(":: Success!")

                    ResultCode.Success
                }

                is Runner.Result.Failure -> {
                    println(":: Failure.")
                    println(result.message)

                    ResultCode.Failure
                }
            }
        }

        is ArgumentsResult.Failure -> {
            println(":: Failure.")
            println(argumentsResult.message)

            ResultCode.Failure
        }
    }

    exitProcess(resultCode.value)
}