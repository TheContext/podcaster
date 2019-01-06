package io.thecontext.podcaster

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException

sealed class ArgumentsResult {
    data class Success(val arguments: Arguments) : ArgumentsResult()
    data class Failure(val message: String) : ArgumentsResult()
}

class Arguments {

    @Parameter(
            names = ["--input"],
            description = "Path to a directory with YAML and Markdown declarations.",
            required = true
    )
    var inputPath: String? = null

    @Parameter(
            names = ["--output-feed"],
            description = "Path to a file which will contain resulting RSS feed.",
            required = true
    )
    var outputFeedPath: String? = null

    @Parameter(
            names = ["--output-website"],
            description = "Path to a directory which will contain resulting Markdown files.",
            required = true
    )
    var outputWebsitePath: String? = null

    companion object {
        fun from(args: Array<String>): ArgumentsResult = try {
            ArgumentsResult.Success(Arguments().apply {
                JCommander(this).apply { programName = "podcaster" }.parse(*args)
            })
        } catch (e: ParameterException) {
            ArgumentsResult.Failure(StringBuilder().apply { e.jCommander.usage(this) }.toString())
        }
    }
}