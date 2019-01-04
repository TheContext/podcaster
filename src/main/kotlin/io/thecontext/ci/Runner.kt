package io.thecontext.ci

import io.reactivex.Observable
import io.reactivex.Single
import io.thecontext.ci.context.Context
import io.thecontext.ci.context.InputContext
import io.thecontext.ci.context.OutputContext
import io.thecontext.ci.context.ValidationContext
import io.thecontext.ci.input.InputFilesLocator
import io.thecontext.ci.validation.ValidationResult
import io.thecontext.ci.validation.merge
import java.io.File

class Runner(
        private val context: Context,
        private val inputDirectory: File,
        private val outputFeedFile: File,
        private val outputWebsiteDirectory: File
) : Runnable {

    override fun run() {
        val inputContext = InputContext.Impl(context)

        val inputFiles = inputContext.inputFilesLocator.locate(inputDirectory)
                .toObservable()
                .share()

        val input = inputFiles
                .ofType<InputFilesLocator.Result.Success>()
                .switchMapSingle {
                    inputContext.inputReader.read(it.people, it.podcast, it.episodes)
                }
                .share()

        val validation = input
                .switchMapSingle { inputResult ->
                    val context = ValidationContext.Impl(context, inputResult.people)

                    val podcast = context.podcastValidator.validate(inputResult.podcast)
                    val episodes = inputResult.episodes.map { context.episodeValidator.validate(it) }
                    val episodeList = context.episodesValidator.validate(inputResult.episodes)

                    Single.merge(episodes.plus(podcast).plus(episodeList)).toList().map { it.merge() }
                }

        val output = validation
                .ofType<ValidationResult.Success>()
                .withLatestFrom(input) { _, inputResult -> inputResult }
                .switchMapSingle {
                    val context = OutputContext.Impl(context)

                    context.outputWriter.write(
                            feedFile = outputFeedFile,
                            websiteDirectory = outputWebsiteDirectory,
                            people = it.people,
                            podcast = it.podcast,
                            episodes = it.episodes
                    )
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