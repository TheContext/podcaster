package io.thecontext.ci

import io.thecontext.ci.context.Context
import java.io.File

fun main(args: Array<String>) {
    val arguments = Arguments.from(args)

    Runner(Context.Impl(), File(arguments.inputPath), File(arguments.outputFeedPath), File(arguments.outputWebsitePath)).run()
}