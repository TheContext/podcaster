package io.thecontext.ci.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import io.thecontext.ci.gradle.task.ValidationTask


open class PodcastPlugin : Plugin<Project>{

    override fun apply(project: Project) {

        project.extensions.create("podcast", PodcastExtension::class.java)
        project.tasks.create("validation", ValidationTask::class.java)

    }
}