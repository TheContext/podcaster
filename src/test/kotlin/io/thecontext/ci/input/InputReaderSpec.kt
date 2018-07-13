package io.thecontext.ci.input

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.*
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.input.InputReader.Result
import io.thecontext.ci.memoized
import io.thecontext.ci.testEpisode
import io.thecontext.ci.testPerson
import io.thecontext.ci.testPodcast
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast
import org.junit.runner.RunWith
import java.io.File

@RunWith(Spectrum::class)
class InputReaderSpec {
    init {
        val env by memoized { Environment() }

        val peopleFile = File("people.yaml")
        val podcastFile = File("podcast.yaml")
        val episodeDir = File("42-kotlin")
        val episodeFile = File(episodeDir, "episode.yaml")
        val episodeDescriptionFile = File(episodeDir, "description.md")

        val podcast = testPodcast
        val person = testPerson
        val episode = testEpisode

        beforeEach {
            env.yamlReader.people = listOf(person)
            env.yamlReader.podcast = podcast
            env.yamlReader.episode = episode
            env.textReader.text = episode.notesMarkdown
        }

        it("emits result") {
            env.reader.read(peopleFile, podcastFile, mapOf(episodeFile to episodeDescriptionFile))
                    .test()
                    .assertResult(Result(podcast, listOf(episode.copy(slug = episodeDir.name)), listOf(person)))
        }
    }

    private class Environment {
        val yamlReader = TestYamlReader()
        val textReader = TestTextReader()

        val reader = InputReader.Impl(
                yamlReader = yamlReader,
                textReader = textReader,
                ioScheduler = Schedulers.trampoline()
        )
    }

    private class TestYamlReader : YamlReader {
        var people: List<Person>? = null
        var podcast: Podcast? = null
        var episode: Episode? = null

        override fun readPeople(file: File) = people ?: throw IllegalStateException("Result is not defined.")
        override fun readPodcast(file: File) = podcast ?: throw IllegalStateException("Result is not defined.")
        override fun readEpisode(file: File) = episode ?: throw IllegalStateException("Result is not defined.")
    }

    private class TestTextReader : TextReader {
        var text: String? = null

        override fun read(file: File) = text ?: throw IllegalStateException("Result is not defined.")
    }
}
