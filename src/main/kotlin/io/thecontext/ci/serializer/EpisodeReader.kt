package io.thecontext.ci.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.model.*
import io.thecontext.ci.model.Link
import io.thecontext.ci.validator.LinkValidator
import org.commonmark.node.*
import org.commonmark.parser.Parser
import java.io.InputStream


abstract class LinkVisitor : Visitor {
    override fun visit(blockQuote: BlockQuote?) {
    }

    override fun visit(bulletList: BulletList?) {
    }

    override fun visit(code: Code?) {
    }

    override fun visit(document: Document?) {
    }

    override fun visit(emphasis: Emphasis?) {
    }

    override fun visit(fencedCodeBlock: FencedCodeBlock?) {
    }

    override fun visit(hardLineBreak: HardLineBreak?) {
    }

    override fun visit(heading: Heading?) {
    }

    override fun visit(thematicBreak: ThematicBreak?) {
    }

    override fun visit(htmlInline: HtmlInline?) {
    }

    override fun visit(htmlBlock: HtmlBlock?) {
    }

    override fun visit(image: Image?) {
    }

    override fun visit(indentedCodeBlock: IndentedCodeBlock?) {
    }


    override fun visit(listItem: ListItem?) {
    }

    override fun visit(orderedList: OrderedList?) {
    }

    override fun visit(paragraph: Paragraph?) {
    }

    override fun visit(softLineBreak: SoftLineBreak?) {
    }

    override fun visit(strongEmphasis: StrongEmphasis?) {
    }

    override fun visit(text: Text?) {
    }

    override fun visit(customBlock: CustomBlock?) {
    }

    override fun visit(customNode: CustomNode?) {
    }
}


/**
 * Reads and validates an Episode file
 */
fun readAndProcessEpisode(objectMapper: ObjectMapper, inputStream: InputStream, persons: Persons, linkValidator: LinkValidator, markdownParser: Parser): Single<Result<ProcessedEpisode>> =
        Single.fromCallable {
            objectMapper.readValue(inputStream, Episode::class.java)
        }.map { episode ->
            val linksFromShowNodes = ArrayList<Link>()

            markdownParser.parse(episode.showNotes).accept(object : LinkVisitor() {
                override fun visit(link: org.commonmark.node.Link) {
                    linksFromShowNodes.add(Link(title = link.title, url = link.destination))
                }
            })
            episode to linksFromShowNodes
        }.flatMap { (episode, linksFromShowNodes) ->


            val guestsResult = persons.personsFromAnnotatedString(episode.guests)
            val hostsResult = persons.personsFromAnnotatedString(episode.hosts)

            val guestLinks = (guestsResult as? ValidResult)?.unwrapValue()?.map(Person::allLinks)?.flatMap { it } ?: emptyList()
            val hostLinks = (hostsResult as? ValidResult)?.unwrapValue()?.map(Person::allLinks)?.flatMap { it } ?: emptyList()

            val allLinks = linksFromShowNodes + episode.additionalLinks + guestLinks + hostLinks
            val linkValidations = allLinks.map { linkValidator.validate(it) }


            Single.merge(linkValidations)
                    .subscribeOn(Schedulers.io())
                    .toList()
                    .map { validatedLinksResult ->
                        validatedLinksResult.partition { it is ErrorResult }.first.flatMap { it.unwrapErrors() }
                    }
                    .map { linkErrors ->

                        val allErrorMessages = ArrayList(linkErrors)

                        if (guestsResult is ErrorResult) {
                            allErrorMessages.addAll(guestsResult.errors)
                        }

                        if (hostsResult is ErrorResult) {
                            allErrorMessages.addAll(hostsResult.errors)
                        } else if (hostsResult.unwrapValue().isEmpty()) {
                            allErrorMessages.add(ErrorMessage("No hosts for this episode specified. Minimum one host is required."))
                        }


                        if (allErrorMessages.isNotEmpty())
                            ErrorResult<ProcessedEpisode>(allErrorMessages)
                        else
                            ValidResult(
                                    ProcessedEpisode(
                                            title = episode.title,
                                            additionalLinks = episode.additionalLinks,
                                            releaseDate = episode.releaseDate,
                                            guests = guestsResult.unwrapValue(),
                                            hosts = hostsResult.unwrapValue()
                                    )
                            )

                    }

        }