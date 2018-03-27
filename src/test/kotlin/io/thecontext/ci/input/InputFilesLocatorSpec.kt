package io.thecontext.ci.input

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.*
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.input.InputFilesLocator.FileNames
import io.thecontext.ci.input.InputFilesLocator.Result
import io.thecontext.ci.memoized
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

        describe("directory is not a directory") {

            it("emits result as failure") {
                locator.locate(File(workingDir.root, "just-a-file"))
                        .test()
                        .assertValue { it is Result.Failure }
            }
        }

        describe("no people file available") {

            beforeEach {
                workingDir.newFile(FileNames.PODCAST)

                val episodeDir = workingDir.newFolder("episode-42")

                listOf(FileNames.EPISODE, FileNames.EPISODE_DESCRIPTION).forEach {
                    File(episodeDir, it).createNewFile()
                }
            }

            it("emits result as failure") {
                locator.locate(workingDir.root)
                        .test()
                        .assertValue { it is Result.Failure }
            }
        }

        describe("no podcast file available") {

            beforeEach {
                workingDir.newFile(FileNames.PEOPLE)

                val episodeDir = workingDir.newFolder("episode-42")

                listOf(FileNames.EPISODE, FileNames.EPISODE_DESCRIPTION).forEach {
                    File(episodeDir, it).createNewFile()
                }
            }

            it("emits result as failure") {
                locator.locate(workingDir.root)
                        .test()
                        .assertValue { it is Result.Failure }
            }
        }

        describe("no episode file available") {

            beforeEach {
                listOf(FileNames.PEOPLE, FileNames.PODCAST).forEach {
                    workingDir.newFile(it)
                }

                val episodeDir = workingDir.newFolder("episode-42")
                File(episodeDir, FileNames.EPISODE_DESCRIPTION).createNewFile()
            }

            it("emits result as failure") {
                locator.locate(workingDir.root)
                        .test()
                        .assertValue { it is Result.Failure }
            }
        }

        describe("no episode description file available") {

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

        describe("files available") {

            beforeEach {
                listOf(FileNames.PEOPLE, FileNames.PODCAST).forEach {
                    workingDir.newFile(it)
                }

                val episodeDir = workingDir.newFolder("episode-42")

                listOf(FileNames.EPISODE, FileNames.EPISODE_DESCRIPTION).forEach {
                    File(episodeDir, it).createNewFile()
                }
            }

            it("emits result as success") {
                locator.locate(workingDir.root)
                        .test()
                        .assertValue { it is Result.Success }
            }
        }

        afterEach {
            workingDir.delete()
        }
    }
}
