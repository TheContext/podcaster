package io.thecontext.ci.gradle

/**
 * Configuration extension for [io.thecontext.ci.gradle.task.ValidationTask]
 */
open class PodcastExtension {

    /**
     * The directory where to find the episode files
     */
    var episodesDir : String ="episodes/"

    /**
     * The path to the itunes.yml file
     */
    var itunesConfig : String ="itunes.yml"

    /**
     * The file containing all peoples
     */
    var people : String = "people.yml"

}