package io.thecontext.ci.context

import io.thecontext.ci.input.InputFilesLocator
import io.thecontext.ci.input.InputReader
import io.thecontext.ci.input.TextReader
import io.thecontext.ci.input.YamlReader

interface InputContext : Context {

    val inputFilesLocator: InputFilesLocator
    val inputReader: InputReader

    class Impl(context: Context) : InputContext, Context by context {

        private val yamlReader by lazy { YamlReader.Impl() }
        private val textReader by lazy { TextReader.Impl() }

        override val inputFilesLocator by lazy { InputFilesLocator.Impl(ioScheduler) }
        override val inputReader by lazy { InputReader.Impl(yamlReader, textReader, ioScheduler) }
    }
}