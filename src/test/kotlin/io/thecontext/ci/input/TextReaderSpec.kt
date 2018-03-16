package io.thecontext.ci.input

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.*
import io.thecontext.ci.memoized
import org.assertj.core.api.Assertions.assertThat
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File

@RunWith(Spectrum::class)
class TextReaderSpec {
    init {
        val workingDir = TemporaryFolder()
        val workingFileName = "42.txt"

        val reader by memoized { TextReader.Impl() }

        beforeEach {
            workingDir.create()
        }

        context("file available") {

            val content = "text file content"

            beforeEach {
                File(workingDir.root, workingFileName).writeText(content, Charsets.UTF_8)
            }

            it("reads as is") {
                assertThat(reader.read(File(workingDir.root, workingFileName))).isEqualTo(content)
            }
        }

        afterEach {
            workingDir.delete()
        }
    }
}