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
        val twitter: String? = null,
        val website: String? = null,
        val github: String? = null
)


val Person.githubLink
    get() = if (this.github != null) Link(title = "github", url = GITHUB_URL + github) else null

val Person.twitterLink
    get() = if (twitter != null) Link(title = "@$twitter", url = TWITTER_URL + twitter) else null

val Person.websiteLink
    get() = if (website != null) Link(title = "website", url = website) else null


/**
 * Get a list of all links for a given person
 */
fun Person.allLinks(): List<Link> {
    val links = ArrayList<Link>()
    twitterLink?.let { links.add(it) }
    websiteLink?.let { links.add(it) }
    githubLink?.let { links.add(it) }
    return links
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





