package io.thecontext.podcaster.input

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.thecontext.podcaster.value.Episode
import io.thecontext.podcaster.value.Person
import io.thecontext.podcaster.value.Podcast
import java.io.File

interface YamlReader {

    fun readPeople(file: File): List<Person>
    fun readPodcast(file: File): Podcast
    fun readEpisode(file: File): Episode

    class Impl : YamlReader {

        private val reader by lazy {
            ObjectMapper(YAMLFactory())
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .registerKotlinModule()
        }

        override fun readPeople(file: File) = reader.readValue(file, Array<Person>::class.java).toList()
        override fun readPodcast(file: File) = reader.readValue(file, Podcast::class.java)
        override fun readEpisode(file: File) = reader.readValue(file, Episode::class.java)
    }
}