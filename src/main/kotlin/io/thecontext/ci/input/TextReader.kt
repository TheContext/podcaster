package io.thecontext.ci.input

import java.io.File

interface TextReader {

    fun read(file: File): String

    class Impl : TextReader {

        override fun read(file: File) = file.readText(Charsets.UTF_8)
    }
}