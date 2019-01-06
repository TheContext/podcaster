package io.thecontext.podcaster.input

import java.io.File

interface TextReader {

    fun read(file: File): String

    class Impl : TextReader {

        override fun read(file: File) = file.readText(Charsets.UTF_8)
    }
}