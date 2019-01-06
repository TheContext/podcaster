package io.thecontext.podcaster

import io.reactivex.Observable
import io.reactivex.Single
import io.thecontext.podcaster.context.Context
import io.thecontext.podcaster.context.InputContext
import io.thecontext.podcaster.context.OutputContext
import io.thecontext.podcaster.context.ValidationContext
import io.thecontext.podcaster.input.InputFilesLocator
import io.thecontext.podcaster.input.InputReader
import io.thecontext.podcaster.validation.ValidationResult
import io.thecontext.podcaster.validation.merge
import java.io.File

interface Runner {

    sealed class Result {
        object Success : Result()
        data class Failure(val message: String) : Result()
    }

    fun run(inputDirectory: File, outputFeedFile: File, outputWebsiteDirectory: File): Single<Result>

    class Impl(private val context: Context) : Runner {

        override fun run(inputDirectory: File, outputFeedFile: File, outputWebsiteDirectory: File): Single<Result> {
            val inputContext = InputContext.Impl(context)

            val inputFiles = inputContext.inputFilesLocator.locate(inputDirectory)
                    .cache()

            val input = inputFiles
                    .flatMap {
                        when (it) {
                            is InputFilesLocator.Result.Success -> inputContext.inputReader.read(it.people, it.podcast, it.episodes)
                            is InputFilesLocator.Result.Failure -> Single.never()
                        }
                    }
                    .cache()

            val validation = input
                    .flatMap {
                        when (it) {
                            is InputReader.Result.Success -> Single.just(it)
                            is InputReader.Result.Failure -> Single.never()
                        }
                    }
                    .flatMap { inputResult ->
                        val context = ValidationContext.Impl(context, inputResult.people)

                        val podcast = context.podcastValidator.validate(inputResult.podcast)
                        val episodes = inputResult.episodes.map { context.episodeValidator.validate(it) }
                        val episodeList = context.episodesValidator.validate(inputResult.episodes)

                        Single.merge(episodes.plus(podcast).plus(episodeList)).toList().map { it.merge() }
                    }
                    .cache()

            val output = validation
                    .flatMap {
                        when (it) {
                            is ValidationResult.Success -> input.cast(InputReader.Result.Success::class.java)
                            is ValidationResult.Failure -> Single.never()
                        }
                    }
                    .flatMap {
                        val context = OutputContext.Impl(context)

                        context.outputWriter.write(
                                feedFile = outputFeedFile,
                                websiteDirectory = outputWebsiteDirectory,
                                people = it.people,
                                podcast = it.podcast,
                                episodes = it.episodes
                        )
                    }

            val resultSuccess = output
                    .map { Result.Success }

            val resultError = Single
                    .merge(
                            inputFiles.flatMap {
                                when (it) {
                                    is InputFilesLocator.Result.Success -> Single.never()
                                    is InputFilesLocator.Result.Failure -> Single.just(it.message)
                                }
                            },
                            input.flatMap {
                                when (it) {
                                    is InputReader.Result.Success -> Single.never()
                                    is InputReader.Result.Failure -> Single.just(it.message)
                                }
                            },
                            validation.flatMap {
                                when (it) {
                                    is ValidationResult.Success -> Single.never()
                                    is ValidationResult.Failure -> Single.just(it.message)
                                }
                            }
                    )
                    .map { Result.Failure(it) }

            return Observable.merge(resultSuccess.toObservable(), resultError.toObservable()).firstOrError()
        }
    }
}