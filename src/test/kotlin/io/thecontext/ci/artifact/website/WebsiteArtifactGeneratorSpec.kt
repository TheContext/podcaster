package io.thecontext.ci.artifact.website

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.afterEach
import com.greghaskins.spectrum.dsl.specification.Specification.beforeEach
import com.greghaskins.spectrum.dsl.specification.Specification.context
import com.greghaskins.spectrum.dsl.specification.Specification.it
import io.reactivex.Single
import io.thecontext.ci.artifact.ArtifactGenerationError
import io.thecontext.ci.artifact.ArtifcatGenerationResult
import io.thecontext.ci.artifact.DeployableArtifact
import io.thecontext.ci.artifact.TextWriter
import io.thecontext.ci.testEpisode
import io.thecontext.ci.testPodcast
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Podcast
import org.junit.Assert
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException

@RunWith(Spectrum::class)
class WebsiteArtifactGeneratorSpec {
    init {

        val podcast = testPodcast
        val tmpDir = File("/tmp/WebsiteArtifactGeneratorSpec/")
        val episode1 = testEpisode.copy(slug = "Episode-1")
        val episode2 = testEpisode.copy(slug = "Episode-2", title = "Episode2")


        afterEach {
            tmpDir.deleteRecursively()
        }

        beforeEach {
            tmpDir.deleteRecursively()
        }

        context("A WebsiteArtifactGenerator") {
            val formatter = JustTitleWebsiteFormatter
            val expectedGeneratedFile1 = File(tmpDir, "${episode1.date}-${episode1.slug}.md")
            val expectedGeneratedFile2 = File(tmpDir, "${episode2.date}-${episode2.slug}.md")
            val expectedGeneratedFileContent1 = JustTitleWebsiteFormatter.format(podcast, episode1).blockingGet()
            val expectedGeneratedFileContent2 = JustTitleWebsiteFormatter.format(podcast, episode2).blockingGet()
            val generator = WebsiteArtifactGenerator.Impl(
                    websiteFormatter = formatter,
                    textWriter = TextWriter.Impl()
            )

            it("generates a artifact consisting of a file named $expectedGeneratedFile1 with content \"$expectedGeneratedFileContent1\""
                    + " and a file $expectedGeneratedFile2.md with content \"$expectedGeneratedFileContent2\"") {
                generator.write(tmpDir, podcast, listOf(episode1, episode2))
                        .test()
                        .assertComplete()
                        .assertResult(ArtifcatGenerationResult.Success(
                                DeployableArtifact.FolderArtifact(tmpDir)
                        ))

                // Check episode1 generated file
                Assert.assertTrue(expectedGeneratedFile1.exists())
                val generatedFileContent1 = expectedGeneratedFile1.readText()
                Assert.assertEquals(expectedGeneratedFileContent1, generatedFileContent1)

                // Check episode2 generated file
                Assert.assertTrue(expectedGeneratedFile2.exists())
                val generatedFileContent2 = expectedGeneratedFile2.readText()
                Assert.assertEquals(expectedGeneratedFileContent2, generatedFileContent2)
            }
        }

        context("A WebsiteArtifactGenerator with an WebsiteFormatter that causes errors") {
            val formatter = ErrorWebsiteFormatter
            val generator = WebsiteArtifactGenerator.Impl(
                    websiteFormatter = formatter,
                    textWriter = TextWriter.Impl()
            )

            it("generates proper errors") {
                generator.write(tmpDir, podcast, listOf(episode1, episode2))
                        .test()
                        .assertComplete()
                        .assertResult(ArtifcatGenerationResult.Failed(
                                listOf(
                                        ArtifactGenerationError("Could not generate Front Matter for episode ${episode1.title}", ErrorWebsiteFormatter.fakeException),
                                        ArtifactGenerationError("Could not generate Front Matter for episode ${episode2.title}", ErrorWebsiteFormatter.fakeException)
                                )
                        ))
            }
        }


        context("A WebsiteArtifactGenerator with an TextWritter that causes errors") {
            val formatter = JustTitleWebsiteFormatter
            val textWriter = ErrorTextWriter
            val generator = WebsiteArtifactGenerator.Impl(
                    websiteFormatter = formatter,
                    textWriter = textWriter
            )

            it("generates proper errors") {
                generator.write(tmpDir, podcast, listOf(episode1, episode2))
                        .test()
                        .assertComplete()
                        .assertResult(ArtifcatGenerationResult.Failed(
                                listOf(
                                        ArtifactGenerationError("Could not generate Front Matter for episode ${episode1.title}", ErrorTextWriter.fakeException),
                                        ArtifactGenerationError("Could not generate Front Matter for episode ${episode2.title}", ErrorTextWriter.fakeException)
                                )
                        ))
            }
        }
    }
}

/**
 *  Mimics Successfully formatting a episode by just printing a
 */
private object JustTitleWebsiteFormatter : WebsiteFormatter {
    override fun format(podcast: Podcast, episode: Episode): Single<String> = Single.fromCallable {
        episode.title
    }
}

/**
 * Mimics Formatter errors
 */
private object ErrorWebsiteFormatter : WebsiteFormatter {
    val fakeException = Exception("Fake Exception")
    override fun format(podcast: Podcast, episode: Episode): Single<String> = Single.error(fakeException)
}

/**
 * Mimics IO Errors
 */
private object ErrorTextWriter : TextWriter {
    val fakeException = IOException("Faked Exception")
    override fun write(file: File, text: String) {
        throw fakeException
    }
}