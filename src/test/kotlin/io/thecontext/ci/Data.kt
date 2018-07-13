package io.thecontext.ci

import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Link
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast

val testPerson = Person(
        id = "Person ID",
        name = "Person Name",
        twitter = "Twitter ID",
        github = "GitHub ID",
        site = "localhost"
)

val testPodcast = Podcast(
        title = "Podcast Title",
        subtitle = "Podcast Subtitle",
        summary = "Podcast summary",
        people = Podcast.People(
                ownerIds = listOf(testPerson.id, testPerson.id),
                authorIds = listOf(testPerson.id, testPerson.id)
        ),
        language = Podcast.Language(
                code = "en",
                regionCode = "us"
        ),
        explicit = false,
        category = "Podcast category",
        subcategory = "Podcast subcategory",
        keywords = listOf("keyword"),
        url = "localhost/podcast",
        artworkUrl = "localhost/podcast/artwork",
        feedUrl = "localhost/podcast/feed"
)

val testEpisodeLink = Link(
        title = "Link Title",
        url = "link.url"
)

val testEpisode = Episode(
        number = 42,
        title = "Title",
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
        notes = Episode.Notes(
                links = listOf(testEpisodeLink),
                descriptionMarkdown = "Episode description"
        )
)