package io.thecontext.ci

import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast

val testPerson = Person(
        id = "Person ID",
        name = "Person Name",
        email = "person@mail.com",
        twitter = "Twitter ID",
        github = "GitHub ID",
        site = "localhost"
)

val testPodcast = Podcast(
        title = "Podcast Title",
        description = "Podcast Description",
        people = Podcast.People(
                ownerId = testPerson.id,
                authorIds = listOf(testPerson.id, testPerson.id)
        ),
        language = Podcast.Language(
                code = "en",
                regionCode = "us"
        ),
        explicit = false,
        category = "Podcast category",
        subcategory = "Podcast subcategory",
        url = "localhost/podcast",
        artworkUrl = "localhost/podcast/artwork"
)

val testEpisode = Episode(
        id = "thecontext/episode/42",
        number = 42,
        title = "Episode Title",
        description = "Episode Description",
        people = Episode.People(
                hostIds = listOf(testPerson.id, testPerson.id),
                guestIds = listOf(testPerson.id, testPerson.id)
        ),
        url = "localhost/episode",
        discussionUrl = "localhost/discussion",
        date = "2000-12-30",
        duration = "100:00",
        file = Episode.File(
                url = "localhost/episode/file",
                length = 1_000_000
        ),
        notesMarkdown = "Notes"
)