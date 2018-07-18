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
import io.thecontext.ci.deployment.DeploymentJob
import io.thecontext.ci.deployment.createDeploymentJob
import io.thecontext.ci.utils.Singles
import io.thecontext.ci.validation.*
import java.io.File

fun main(args: Array<String>) {
    val result = Runner().run(File("/tmp/podcast-input"))
    System.exit(result.exitCode)
}

class Runner {
    enum class ReturnType(val exitCode: Int) {
        SUCCESS(0),
        FAILURE(1)
    }

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
    private val feedAndShowNotesArtifactGenerator: FeedAndShowNotesArtifactGenerator by lazy { FeedAndShowNotesArtifactGenerator(rssFormatter, episodeMarkdownFormatter, textWriter, ioScheduler, File("/tmp/podcast-artifact")) }
    private val websiteArtifactGenerator: WebsiteArtifactGenerator by lazy { WebsiteArtifactGenerator(WebsiteFormatter.Impl(mustacheRenderer, Schedulers.io()), textWriter, File("/tmp/podcast-website")) }
    private val artifactGenerators: List<ArtifactGenerator> by lazy { listOf(feedAndShowNotesArtifactGenerator, websiteArtifactGenerator) }

    fun run(inputDirectory: File): ReturnType {

        // TODO refactor the whole pipeline to one gian Single<List<ConsoleOutput>>

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


        val deployment: Observable<List<ConsoleOutput>> = validation
                .ofType<ValidationResult.Success>()
                .withLatestFrom(input.ofType<InputReader.Result.Success>()) { _, inputResult -> inputResult }
                .switchMapSingle { (podcast, episodes) ->
                    val artifactTasks: List<Single<ArtifactGenerationResult>> = artifactGenerators
                            .map { it.generateArtifact(podcast, episodes) }

                    val consoleOutputs: Single<List<ConsoleOutput>> = Singles.zip(artifactTasks).flatMap { artifactGenerationResults ->
                        // only start deployment if all artifacts has been generated
                        val (success, error) = artifactGenerationResults.partition {
                            when (it) {
                                is ArtifactGenerationResult.Success -> true
                                is ArtifactGenerationResult.Failure -> false
                            }
                        }

                        if (error.isEmpty()) {
                            // Do deployment
                            val artifactsToDeploy = success.map { (it as ArtifactGenerationResult.Success).artifact }
                            val deploymentJobs = artifactsToDeploy.map(::createDeploymentJob)
                            val deploymentTasks: List<Single<DeploymentJob.Result>> = deploymentJobs.map { it.deploy() }

                            Singles.zip(deploymentTasks) { deploymentResults ->
                                deploymentResults.map {
                                    when (it) {
                                        is DeploymentJob.Result.Success -> ConsoleOutput(type = ConsoleOutput.Type.INFO, message = it.message)
                                        is DeploymentJob.Result.Failure -> ConsoleOutput(type = ConsoleOutput.Type.ERROR, message = it.message)
                                    }
                                }
                            }
                        } else {
                            // Errors, don't deploy!
                            val errorMessages: List<ConsoleOutput> = error.flatMap {
                                (it as ArtifactGenerationResult.Failure)
                                        .errors
                                        .map { ConsoleOutput(type = ConsoleOutput.Type.ERROR, message = it.message) }
                            }
                            Single.just(errorMessages)
                        }
                    }
                    consoleOutputs

                }

        val resultError: Observable<List<ConsoleOutput>> = Observable
                .merge(
                        inputFiles.ofType<InputFilesLocator.Result.Failure>().map { listOf(ConsoleOutput(ConsoleOutput.Type.ERROR, it.message)) },
                        input.ofType<InputReader.Result.Failure>().map { listOf(ConsoleOutput(ConsoleOutput.Type.ERROR, it.message)) },
                        validation.ofType<ValidationResult.Failure>().map { listOf(ConsoleOutput(ConsoleOutput.Type.ERROR, it.message)) }
                )

        val consoleOutputs: List<ConsoleOutput> = Observable.merge(deployment, resultError)
                .toList()
                .blockingGet()
                .flatMap { it }

        consoleOutputs.forEach { it.print() }

        return if (consoleOutputs.find { ConsoleOutput.Type.ERROR == it.type } != null) {
            ReturnType.FAILURE
        } else {
            ReturnType.SUCCESS
        }
    }
}

