package io.thecontext.ci

import io.thecontext.ci.context.Context
import java.io.File

fun main(args: Array<String>) {
    val arguments = Arguments.from(args)
    val runner = Runner.Impl(Context.Impl())

    val inputDirectory = File(arguments.inputPath)
    val outputFeedFile = File(arguments.outputFeedPath)
    val outputWebsiteDirectory = File(arguments.outputWebsitePath)

    val result = runner.run(inputDirectory, outputFeedFile, outputWebsiteDirectory).blockingGet()

    when (result) {
        is Runner.Result.Success -> println(":: Success!")
        is Runner.Result.Failure -> listOf(":: Failure.", result.message).forEach { println(it) }
    }
}