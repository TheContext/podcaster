package io.thecontext.ci.model

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertEquals
import kotlin.test.fail

class PersonFromAnnotatedStringTest : Spek({

    val ArtemZinnatullin = Person(
            id = "ArtemZinnatullin",
            name = "Artem Zinnatullin",
            twitter = "artem_zin",
            github = "artem-zinnatullin",
            website = "https://artemzin.com"
    )

    val HannesDorfmann = Person(
            id = "HannesDorfmann",
            name = "Hannes Dorfmann",
            twitter = "sockeqwe",
            github = "sockeqwe",
            website = "http://hannesdorfmann.com"
    )

    val persons: Persons = mapOf(ArtemZinnatullin.id to ArtemZinnatullin, HannesDorfmann.id to HannesDorfmann)


    describe("A empty list of annotated persons") {
        it("should return an empty list of persons") {
            val result = persons.personsFromAnnotatedString(emptyList())
            assertEquals(ValidResult(emptyList()), result)
        }
    }

    describe("A list with 2 annotated persons") {
        it("should return an list of 2 Persons") {
            val result = persons.personsFromAnnotatedString(listOf(ArtemZinnatullin.id, HannesDorfmann.id))
            assertEquals(ValidResult(listOf(ArtemZinnatullin, HannesDorfmann)), result)
        }
    }

    describe("A list with annotated persons") {
        it("should report an Error if the annotated person is missing in the Persons Map") {
            val result = persons.personsFromAnnotatedString(listOf(ArtemZinnatullin.id, HannesDorfmann.id, "UnknownPerson"))
            assertEquals(ErrorResult("No person with id 'UnknownPerson' found. Have you specified such a person in persons file?"), result)
        }
    }


})