package io.thecontext.ci

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.input.InputFilesLocator
import io.thecontext.ci.input.InputReader
import io.thecontext.ci.input.TextReader
import io.thecontext.ci.input.YamlReader
import io.thecontext.ci.output.*
import io.thecontext.ci.validation.*
import java.io.File

fun main(args: Array<String>) {
    Runner().run(File("/tmp/podcast-input"), File("/tmp/podcast-output"))
}

class Runner {

    private val ioScheduler by lazy { Schedulers.io() }

    private val yamlReader by lazy { YamlReader.Impl() }
    private val textReader by lazy { TextReader.Impl() }
    private val inputFilesLocator by lazy { InputFilesLocator.Impl(ioScheduler) }
    private val inputReader by lazy { InputReader.Impl(yamlReader, textReader, ioScheduler) }

    private val urlValidator by lazy { UrlValidator(ioScheduler) }

    private val markdownRenderer by lazy { MarkdownRenderer.Impl() }
    private val mustacheRenderer by lazy { MustacheRenderer.Impl() }
    private val textWriter by lazy { TextWriter.Impl() }
    private val episodeMarkdownFormatter by lazy { EpisodeMarkdownFormatter.Impl(mustacheRenderer, ioScheduler) }
    private val podcastXmlFormatter by lazy { PodcastXmlFormatter.Impl(episodeMarkdownFormatter, markdownRenderer, mustacheRenderer, ioScheduler) }
    private val outputWriter by lazy { OutputWriter.Impl(podcastXmlFormatter, episodeMarkdownFormatter, textWriter, ioScheduler) }

    fun run(inputDirectory: File, outputDirectory: File) {
        val inputFiles = inputFilesLocator.locate(inputDirectory)
                .toObservable()
                .share()

        val input = inputFiles
                .ofType<InputFilesLocator.Result.Success>()
                .switchMapSingle {
                    inputReader.read(it.people, it.podcast, it.episodes)
                }
                .share()

        val validation = input
                .switchMapSingle { inputResult ->
                    val podcast = PodcastValidator(urlValidator, inputResult.people).validate(inputResult.podcast)
                    val episodes = inputResult.episodes.map { EpisodeValidator(urlValidator, inputResult.people).validate(it) }
                    val episodeList = EpisodeListValidator(ioScheduler).validate(inputResult.episodes)

                    Single.merge(episodes.plus(podcast).plus(episodeList)).toList().map { it.merge() }
                }

        val output = validation
                .ofType<ValidationResult.Success>()
                .withLatestFrom(input) { _, inputResult -> inputResult }
                .switchMapSingle {
                    outputWriter.write(outputDirectory, it.podcast, it.episodes, it.people)
                }

        val resultSuccess = output.map { "Done!" }

        val resultError = Observable
                .merge(
                        inputFiles.ofType<InputFilesLocator.Result.Failure>().map { it.message },
                        validation.ofType<ValidationResult.Failure>().map { it.message }
                )

        println(Observable.merge(resultSuccess, resultError).blockingFirst())
    }
}