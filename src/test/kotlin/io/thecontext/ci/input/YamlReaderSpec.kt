package io.thecontext.ci.input

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.*
import io.thecontext.ci.memoized
import io.thecontext.ci.testEpisode
import io.thecontext.ci.testPerson
import io.thecontext.ci.testPodcast
import io.thecontext.ci.value.Episode
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
                      twitter: ${person.twitter}
                      github: ${person.github}
                      website: ${person.site}
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
                    subtitle: ${podcast.subtitle}
                    summary: ${podcast.summary}
                    peopleIds:
                        owners:
                            - ${podcast.people.ownerIds[0]}
                            - ${podcast.people.ownerIds[1]}
                        authors:
                            - ${podcast.people.authorIds[0]}
                            - ${podcast.people.authorIds[1]}
                    language:
                        code: ${podcast.language.code}
                        regionCode: ${podcast.language.regionCode}
                    explicit: ${podcast.explicit}
                    category: ${podcast.category}
                    subcategory: ${podcast.subcategory}
                    keywords:
                        - ${podcast.keywords.first()}
                    url: ${podcast.url}
                    artworkUrl: ${podcast.artworkUrl}
                    feedUrl: ${podcast.feedUrl}
                    """

            beforeEach {
                File(workingDir.root, workingFileName).writeText(podcastYaml)
            }

            it("reads as is") {
                assertThat(reader.readPodcast(File(workingDir.root, workingFileName))).isEqualTo(podcast)
            }
        }

        describe("episode") {
            val episode = testEpisode.copy(notes = Episode.Notes(links = testEpisode.notes.links))
            val episodeYaml =
                    """
                    number: ${episode.number}
                    title: ${episode.title}
                    date: ${episode.date}
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
                    notes:
                        links:
                            - title: ${episode.notes.links.first().title}
                              url: ${episode.notes.links.first().url}
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
