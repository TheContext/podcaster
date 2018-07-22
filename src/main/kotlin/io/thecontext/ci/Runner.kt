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
    Runner().run(
            inputDirectory = File("/tmp/podcast-input"),
            rssFeedDirectory = File("/tmp/podcast-output"),
            showNotesDirectory = File("/tmp/podcast-output"),
            websiteDirectory = File("/tmp/podcast-website")
    )
}

class Runner {

    private val ioScheduler by lazy { Schedulers.io() }

    private val yamlReader: YamlReader by lazy { YamlReader.Impl() }
    private val textReader: TextReader by lazy { TextReader.Impl() }
    private val inputFilesLocator: InputFilesLocator by lazy { InputFilesLocator.Impl(ioScheduler) }
    private val inputReader: InputReader by lazy { InputReader.Impl(yamlReader, textReader, ioScheduler) }

    private val urlValidator: UrlValidator by lazy { UrlValidator(ioScheduler) }

    private val markdownRenderer: MarkdownRenderer by lazy { MarkdownRenderer.Impl() }
    private val mustacheRenderer: MustacheRenderer by lazy { MustacheRenderer.Impl() }
    private val textWriter: TextWriter by lazy { TextWriter.Impl() }
    private val episodeMarkdownFormatter: EpisodeMarkdownFormatter by lazy { EpisodeMarkdownFormatter.Impl(mustacheRenderer, ioScheduler) }
    private val podcastXmlFormatter: PodcastXmlFormatter by lazy { PodcastXmlFormatter.Impl(episodeMarkdownFormatter, markdownRenderer, mustacheRenderer, ioScheduler) }
    private val websiteFormatter: WebsiteFormatter by lazy { WebsiteFormatter.Impl(mustacheRenderer, ioScheduler) }
    private val outputWriter: OutputWriter by lazy { OutputWriter.Impl(podcastXmlFormatter, episodeMarkdownFormatter, websiteFormatter, textWriter, ioScheduler) }

    fun run(inputDirectory: File, showNotesDirectory: File, rssFeedDirectory: File, websiteDirectory: File) {
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

                    Single.merge(episodes.plus(podcast)).toList().map { it.merge() }
                }

        val output = validation
                .ofType<ValidationResult.Success>()
                .withLatestFrom(input) { _, inputResult -> inputResult }
                .switchMapSingle {
                    outputWriter.write(
                            showNotesDirectory = showNotesDirectory,
                            rssFeedDirectory = rssFeedDirectory,
                            websiteDirectory = websiteDirectory,
                            people = it.people,
                            podcast = it.podcast,
                            episodes = it.episodes)
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