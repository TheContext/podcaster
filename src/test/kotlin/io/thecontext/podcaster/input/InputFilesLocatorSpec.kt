package io.thecontext.podcaster.input

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.*
import io.reactivex.schedulers.Schedulers
import io.thecontext.podcaster.input.InputFilesLocator.FileNames
import io.thecontext.podcaster.input.InputFilesLocator.Result
import io.thecontext.podcaster.memoized
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File

@RunWith(Spectrum::class)
class InputFilesLocatorSpec {
    init {
        val workingDir = TemporaryFolder()

        val locator by memoized { InputFilesLocator.Impl(Schedulers.trampoline()) }

        beforeEach {
            workingDir.create()
        }

        describe("files available") {

            beforeEach {
                listOf(FileNames.PEOPLE, FileNames.PODCAST).forEach {
                    workingDir.newFile(it)
                }

                val episodeDir = workingDir.newFolder("episode-42")

                listOf(FileNames.EPISODE, FileNames.EPISODE_NOTES).forEach {
                    File(episodeDir, it).createNewFile()
                }
            }

            it("emits result as success") {
                locator.locate(workingDir.root)
                        .test()
                        .assertValue { it is Result.Success }
            }
        }

        describe("directory does not exist") {

            it("emits result as failure") {
                val dir = File("does-not-actually-exist")

                locator.locate(dir)
                        .test()
                        .assertValue { it is Result.Failure }
            }
        }

        describe("directory is not a directory") {

            it("emits result as failure") {
                val file = workingDir.newFile("podcast")

                locator.locate(file)
                        .test()
                        .assertValue { it is Result.Failure }
            }
        }

        describe("people file is not available") {

            beforeEach {
                workingDir.newFile(FileNames.PODCAST)

                val episodeDir = workingDir.newFolder("episode-42")

                listOf(FileNames.EPISODE, FileNames.EPISODE_NOTES).forEach {
                    File(episodeDir, it).createNewFile()
                }
            }

            it("emits result as failure") {
                locator.locate(workingDir.root)
                        .test()
                        .assertValue { it is Result.Failure }
            }
        }

        describe("podcast file is not available") {

            beforeEach {
                workingDir.newFile(FileNames.PEOPLE)

                val episodeDir = workingDir.newFolder("episode-42")

                listOf(FileNames.EPISODE, FileNames.EPISODE_NOTES).forEach {
                    File(episodeDir, it).createNewFile()
                }
            }

            it("emits result as failure") {
                locator.locate(workingDir.root)
                        .test()
                        .assertValue { it is Result.Failure }
            }
        }

        describe("episode file is not available") {

            beforeEach {
                listOf(FileNames.PEOPLE, FileNames.PODCAST).forEach {
                    workingDir.newFile(it)
                }

                val episodeDir = workingDir.newFolder("episode-42")
                File(episodeDir, FileNames.EPISODE_NOTES).createNewFile()
            }

            it("emits result as failure") {
                locator.locate(workingDir.root)
                        .test()
                        .assertValue { it is Result.Failure }
            }
        }

        describe("episode notes file is not available") {

            beforeEach {
                listOf(FileNames.PEOPLE, FileNames.PODCAST).forEach {
                    workingDir.newFile(it)
                }

                val episodeDir = workingDir.newFolder("episode-42")

                File(episodeDir, FileNames.EPISODE).createNewFile()
            }

            it("emits result as failure") {
                locator.locate(workingDir.root)
                        .test()
                        .assertValue { it is Result.Failure }
            }
        }

        afterEach {
            workingDir.delete()
        }
    }
}
