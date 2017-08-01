package io.thecontext.ci.gradle.task

import io.thecontext.ci.gradle.PodcastPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ValidationTask : DefaultTask(){

    @TaskAction
    fun validate(){
        val config = project.extensions.findByType(PodcastPlugin::class.java) ?: PodcastPlugin()

    }
}