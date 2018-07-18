package io.thecontext.ci.artifact

import com.github.mustachejava.DefaultMustacheFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringWriter

interface MustacheRenderer {

    fun render(templateResourceName: String, contents: Map<String, Any>): String

    class Impl : MustacheRenderer {

        private val factory by lazy { DefaultMustacheFactory() }

        override fun render(templateResourceName: String, contents: Map<String, Any>): String {
            val templateReader = BufferedReader(InputStreamReader(this.javaClass.getResourceAsStream("/$templateResourceName")))
            val template = factory.compile(templateReader, templateResourceName)

            return with(StringWriter()) {
                template.execute(this, contents)

                toString()
            }
        }
    }
}