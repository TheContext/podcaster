package io.thecontext.ci.gradle

import io.thecontext.ci.gradle.task.ValidationTask
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.Assert

class PodcastPluginTest : Spek({

    describe("podcast plugin") {
        it("should apply validation task") {
            val project = ProjectBuilder.builder().build()
            project.plugins.apply("io.thecontext.ci.podcast")

            Assert.assertTrue(project.tasks.getByName("validation") is ValidationTask)
        }
    }
})