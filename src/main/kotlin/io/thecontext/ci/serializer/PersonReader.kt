package io.thecontext.ci.serializer

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.reactivex.Single
import io.thecontext.ci.model.*
import java.io.InputStream

/**
 * Reads the persons.yml file
 */
fun readPersons(objectMapper: ObjectMapper, personsInputStream: InputStream): Single<Result<Persons>> = Single.fromCallable {
    // TODO check if person twitter is unique, website is unique, etc.

    try {
        val personListRef = object : TypeReference<List<Person>>() {};
        val parsedPersons: List<Person> = objectMapper.readValue(personsInputStream, personListRef)

        val (duplicatePersons, validPersons) = parsedPersons.groupBy { it.id }
                .toList()
                .partition { (_, persons) -> persons.size > 1 }

        if (duplicatePersons.isNotEmpty()) {
            val detailsMessage = duplicatePersons.fold(StringBuilder()) { builder, (id, persons) ->
                builder.append("\n").append("\tid: ").append(id).append(" -> ").append(persons)
            }.toString()

            ErrorResult<Persons>("Person id must be unique. The following persons have the same id:$detailsMessage")
        } else {

            val persons: Persons = validPersons.fold(HashMap<String, Person>()) { map, (id, personListWithOneEntry) ->
                map[id] = personListWithOneEntry[0]
                map
            }
            ValidResult(persons)
        }
    } catch (t: MismatchedInputException) {
        ErrorResult<Persons>(listOf(ErrorMessage("File error", t)))
    }
}
