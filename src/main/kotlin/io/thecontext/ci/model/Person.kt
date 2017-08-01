package io.thecontext.ci.model


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


