package io.thecontext.ci

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.input.InputFilesLocator
import io.thecontext.ci.input.InputReader
import io.thecontext.ci.input.TextReader
import io.thecontext.ci.input.YamlReader
import io.thecontext.ci.artifact.*
import io.thecontext.ci.artifact.feedandshownotes.EpisodeMarkdownFormatter
import io.thecontext.ci.artifact.feedandshownotes.FeedAndShowNotesArtifactGenerator
import io.thecontext.ci.artifact.feedandshownotes.MarkdownRenderer
import io.thecontext.ci.artifact.feedandshownotes.RssFormatter
import io.thecontext.ci.artifact.website.WebsiteArtifactGenerator
import io.thecontext.ci.artifact.website.WebsiteFormatter
import io.thecontext.ci.validation.*
import java.io.File

fun main(args: Array<String>) {
    Runner().run(File("/tmp/podcast-input"), File("/tmp/podcast-artifact"))
}

class Runner {

    private val ioScheduler by lazy { Schedulers.io() }

    private val yamlReader: YamlReader by lazy { YamlReader.Impl() }
    private val textReader: TextReader by lazy { TextReader.Impl() }
    private val inputFilesLocator: InputFilesLocator by lazy { InputFilesLocator.Impl(ioScheduler) }
    private val inputReader: InputReader by lazy { InputReader.Impl(yamlReader, textReader, ioScheduler) }

    private val urlValidator by lazy { UrlValidator(ioScheduler) }
    private val podcastValidator by lazy { PodcastValidator(urlValidator) }
    private val episodeValidator by lazy { EpisodeValidator(urlValidator) }

    private val markdownRenderer: MarkdownRenderer by lazy { MarkdownRenderer.Impl() }
    private val mustacheRenderer: MustacheRenderer by lazy { MustacheRenderer.Impl() }
    private val textWriter: TextWriter by lazy { TextWriter.Impl() }
    private val episodeMarkdownFormatter: EpisodeMarkdownFormatter by lazy { EpisodeMarkdownFormatter.Impl(mustacheRenderer, ioScheduler) }
    private val rssFormatter: RssFormatter by lazy { RssFormatter.Impl(episodeMarkdownFormatter, markdownRenderer, mustacheRenderer, ioScheduler) }
    private val feedAndShowNotesArtifactGenerator: FeedAndShowNotesArtifactGenerator by lazy { FeedAndShowNotesArtifactGenerator.Impl(rssFormatter, episodeMarkdownFormatter, textWriter, ioScheduler) }
    private val websiteArtifactGenerator: WebsiteArtifactGenerator by lazy { WebsiteArtifactGenerator.Impl(WebsiteFormatter.Impl(mustacheRenderer, Schedulers.io()), textWriter) }

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
                .ofType<InputReader.Result.Success>()
                .switchMapSingle { inputResult ->
                    val podcast = podcastValidator.validate(inputResult.podcast)
                    val episodes = inputResult.episodes.map { episodeValidator.validate(it) }

                    Single.merge(episodes.plus(podcast)).toList().map { it.merge() }
                }

        val output = validation
                .ofType<ValidationResult.Success>()
                .withLatestFrom(input.ofType<InputReader.Result.Success>()) { _, inputResult -> inputResult }
                .switchMapSingle {
                    feedAndShowNotesArtifactGenerator.write(outputDirectory, it.podcast, it.episodes)
                }

        val resultSuccess = output.map { "Done!" }

        val resultError = Observable
                .merge(
                        inputFiles.ofType<InputFilesLocator.Result.Failure>().map { it.message },
                        input.ofType<InputReader.Result.Failure>().map { it.message },
                        validation.ofType<ValidationResult.Failure>().map { it.message }
                )

        println(Observable.merge(resultSuccess, resultError).blockingFirst())
    }
}