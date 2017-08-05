package io.thecontext.ci.model

private val GITHUB_URL = "https://github.com/"
private val TWITTER_URL = "https://twitter.com/"


/**
 * Just a simple type alias for convenience
 */
typealias Persons = Map<String, Person>


/**
 * Contains information about a certain person
 */
data class Person(
        val id: String,
        val name: String,
        private val twitter: String? = null,
        private val website: String? = null,
        private val github: String? = null
) {


    val githubLink = if (this.github != null) Link(title = "github", url = GITHUB_URL + github) else null

    val twitterLink = if (twitter != null) Link(title = "@$twitter", url = TWITTER_URL + twitter) else null

    val websiteLink = if (website != null) Link(title = "website", url = website) else null

    /**
     * Get a list of all links for a given person
     */
    val allLinks: List<Link> = arrayOf(twitterLink, websiteLink, githubLink).filterNotNull()

}


private val PERSON_ANNOTATION = Regex("^\\w+\$")


/**
 * Get a list of [Person] from a list of annotated strings (person ids) referencing a person.
 */
fun Persons.personsFromAnnotatedString(annotatedPersons: List<String>): Result<List<Person>> {

    val (errors, persons) = annotatedPersons.map {

        val annotation = it.trim()
        if (PERSON_ANNOTATION.matches(annotation)) {
            val person = this[annotation]
            if (person == null)
                ErrorMessageHolder(ErrorMessage("No person with id '$annotation' found. Have you specified such a person in persons file?"))
            else
                PersonHolder(person)
        } else {
            ErrorMessageHolder(ErrorMessage("Person with id '$it' is invalid. Must match Regex ${PERSON_ANNOTATION.pattern}."))
        }
    }.partition { it is ErrorMessageHolder }

    return if (errors.isNotEmpty()) {
        ErrorResult(errors.map { (it as ErrorMessageHolder).errorMessage })
    } else
        ValidResult(persons.map { (it as PersonHolder).person })

}

// I really wish kotlin would support union types
private sealed class PersonOrErrorMessage
private data class PersonHolder(val person: Person) : PersonOrErrorMessage()
private data class ErrorMessageHolder(val errorMessage: ErrorMessage) : PersonOrErrorMessage()