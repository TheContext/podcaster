package io.thecontext.ci.context

import io.thecontext.ci.input.InputFilesLocator
import io.thecontext.ci.input.InputReader
import io.thecontext.ci.input.TextReader
import io.thecontext.ci.input.YamlReader

interface InputContext : Context {

    val inputFilesLocator: InputFilesLocator
    val inputReader: InputReader

    class Impl(context: Context) : InputContext, Context by context {

        private val yamlReader: YamlReader by lazy { YamlReader.Impl() }
        private val textReader: TextReader by lazy { TextReader.Impl() }

        override val inputFilesLocator: InputFilesLocator by lazy { InputFilesLocator.Impl(ioScheduler) }
        override val inputReader: InputReader by lazy { InputReader.Impl(yamlReader, textReader, ioScheduler) }
    }
}