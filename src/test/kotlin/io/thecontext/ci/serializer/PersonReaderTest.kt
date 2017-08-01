package io.thecontext.ci.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.thecontext.ci.model.Person
import io.thecontext.ci.loadResourceFile
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertEquals
import kotlin.test.fail

class PersonReaderTest : Spek({

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

    val objectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

/*
    describe("An empty persons list") {
        it("should return an empty Persons map") {
            assertEquals(emptyMap(), readPersons(objectMapper, loadResourceFile("persons/EmptyPersons.yml")).blockingGet())
        }
    }
*/

    describe("A valid person list") {
        it("should return a valid persons map") {
            val expected = mapOf("ArtemZinnatullin" to ArtemZinnatullin, "HannesDorfmann" to HannesDorfmann)
            val persons = readPersons(objectMapper, loadResourceFile("persons/ValidPersons.yml")).blockingGet()
            assertEquals(expected, persons)
        }
    }


    describe("A person list with duplicate ids") {
        it("should fail") {
            try {
                readPersons(objectMapper, loadResourceFile("persons/DuplicatePersons.yml")).blockingGet()
                fail("Exception expected")
            } catch(e : Exception){
                // TODO string comparison of error message
            }
        }
    }

})