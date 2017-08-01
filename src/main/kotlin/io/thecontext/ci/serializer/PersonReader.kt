package io.thecontext.ci.serializer

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.Single
import io.thecontext.ci.model.Person
import io.thecontext.ci.model.Persons
import java.io.File
import java.io.InputStream

/**
 * Reads the persons.yml file
 */
fun readPersons(objectMapper: ObjectMapper, personsInputStream : InputStream): Single<Persons> = Single.create {

    val personListRef = object : TypeReference<List<Person>>() {};
    val parsedPersons: List<Person> = objectMapper.readValue(personsInputStream, personListRef)

    val (duplicatePersons, validPersons) = parsedPersons.groupBy { it.id }
            .toList()
            .partition { (_, persons) -> persons.size > 1 }

    if (!it.isDisposed) {

        if (duplicatePersons.isNotEmpty()) {
            val detailsMessage = duplicatePersons.foldIndexed(StringBuilder()) { i, builder, dulicates ->
                val (id, persons) = dulicates
                if (i > 1)
                    builder.append("\n")

                builder.append("id: ").append(id).append(" -> ").append(persons)
            }.toString()

            it.onError(Exception("Person id must be unique. The following persons have the same id:\n$detailsMessage"))
        }

        val persons: Persons = validPersons.fold(HashMap<String, Person>()) { map, (id, personListWithOneEntry) ->
            map[id] = personListWithOneEntry[0]
            map
        }
        it.onSuccess(persons)
    }
}