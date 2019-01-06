package io.thecontext.podcaster.validation

import io.reactivex.Scheduler
import io.reactivex.Single
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Heading
import org.commonmark.parser.Parser
import java.util.concurrent.atomic.AtomicBoolean

class MarkdownValidator(
        private val ioScheduler: Scheduler
) : Validator<String> {

    private val markdownParser by lazy { Parser.builder().build() }

    override fun validate(value: String) = Single
            .fromCallable {
                val headingsAvailable = AtomicBoolean(false)

                markdownParser.parse(value).accept(ValidationVisitor(headingsAvailable))

                if (headingsAvailable.get()) {
                    ValidationResult.Failure("Markdown headings are not supported since they don’t render correctly in RSS feed.")
                } else {
                    ValidationResult.Success
                }
            }
            .subscribeOn(ioScheduler)

    private class ValidationVisitor(
            private val headingsAvailable: AtomicBoolean
    ) : AbstractVisitor() {

        override fun visit(heading: Heading) {
            headingsAvailable.set(true)

            super.visit(heading)
        }
    }
}