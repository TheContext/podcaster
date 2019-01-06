package io.thecontext.podcaster.output

import java.io.File

interface TextWriter {

    fun write(file: File, text: String)

    class Impl : TextWriter {

        override fun write(file: File, text: String) = file.writeText(text, Charsets.UTF_8)
    }
}