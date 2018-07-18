package io.thecontext.ci

/**
 * Simple messages that cen be printed as console output.
 * To distinguish which kind of Output it is we have [Type]
 */
data class ConsoleOutput(val type: Type, val message: String) {
    enum class Type {
        INFO,
        ERROR
    }
}

fun ConsoleOutput.print() {
    // TODO: make the Stream we write to pass as parameter
    println(message)
}