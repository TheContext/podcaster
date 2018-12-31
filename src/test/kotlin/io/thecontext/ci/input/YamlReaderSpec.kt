package io.thecontext.ci.input

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.*
import io.thecontext.ci.memoized
import io.thecontext.ci.testEpisode
import io.thecontext.ci.testPerson
import io.thecontext.ci.testPodcast
import org.assertj.core.api.Assertions.assertThat
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File

@RunWith(Spectrum::class)
class YamlReaderSpec {
    init {
        val workingDir = TemporaryFolder()
        val workingFileName = "42.yaml"

        val reader by memoized { YamlReader.Impl() }

        beforeEach {
            workingDir.create()
        }

        describe("people") {

            val person = testPerson
            val peopleYaml =
                    """
                    - id: ${person.id}
                      name: ${person.name}
                      email: ${person.email}
                      twitter: ${person.twitter}
                      github: ${person.github}
                      links:
                          - name: ${person.links.first().name}
                            url: ${person.links.first().url}
                    """

            beforeEach {
                File(workingDir.root, workingFileName).writeText(peopleYaml)
            }

            it("reads as is") {
                assertThat(reader.readPeople(File(workingDir.root, workingFileName))).isEqualTo(listOf(person))
            }
        }

        describe("podcast") {

            val podcast = testPodcast
            val podcastYaml =
                    """
                    title: ${podcast.title}
                    description: ${podcast.description}
                    peopleIds:
                        owner: ${podcast.people.ownerId}
                        authors:
                            - ${podcast.people.authorIds[0]}
                            - ${podcast.people.authorIds[1]}
                    language: ${podcast.language}
                    explicit: ${podcast.explicit}
                    category: ${podcast.category}
                    subcategory: ${podcast.subcategory}
                    url: ${podcast.url}
                    artworkUrl: ${podcast.artworkUrl}
                    """

            beforeEach {
                File(workingDir.root, workingFileName).writeText(podcastYaml)
            }

            it("reads as is") {
                assertThat(reader.readPodcast(File(workingDir.root, workingFileName))).isEqualTo(podcast)
            }
        }

        describe("episode") {
            val episode = testEpisode.copy(notesMarkdown = "")
            val episodeYaml =
                    """
                    id: ${episode.id}
                    number: ${episode.number}
                    part: ${episode.part}
                    title: ${episode.title}
                    description: ${episode.description}
                    time: ${episode.time}
                    duration: ${episode.duration}
                    peopleIds:
                        hosts:
                            - ${episode.people.hostIds[0]}
                            - ${episode.people.hostIds[1]}
                        guests:
                            - ${episode.people.guestIds[0]}
                            - ${episode.people.guestIds[1]}
                    url: ${episode.url}
                    discussionUrl: ${episode.discussionUrl}
                    file:
                        url: ${episode.file.url}
                        length: ${episode.file.length}
                    """

            beforeEach {
                File(workingDir.root, workingFileName).writeText(episodeYaml)
            }

            it("reads as is") {
                assertThat(reader.readEpisode(File(workingDir.root, workingFileName))).isEqualTo(episode)
            }
        }

        afterEach {
            workingDir.delete()
        }
    }
}
