package io.thecontext.ci

import io.thecontext.ci.context.Context
import java.io.File

enum class ResultCode(val value: Int) {
    Success(0),
    Failure(1),
}

fun main(args: Array<String>) {
    val arguments = Arguments.from(args)
    val runner = Runner.Impl(Context.Impl())

    val inputDirectory = File(arguments.inputPath)
    val outputFeedFile = File(arguments.outputFeedPath)
    val outputWebsiteDirectory = File(arguments.outputWebsitePath)

    val result = runner.run(inputDirectory, outputFeedFile, outputWebsiteDirectory).blockingGet()

    val resultCode = when (result) {
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

    System.exit(resultCode.value)
}