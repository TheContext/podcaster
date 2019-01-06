package io.thecontext.podcaster.validation

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.podcaster.value.Episode

class EpisodesValidator(
        private val ioScheduler: Scheduler
) : Validator<List<Episode>> {

    override fun validate(value: List<Episode>) = Single
            .fromCallable {
                val duplicateIds = value
                        .groupingBy { it.id }
                        .eachCount()
                        .filter { (_, count) -> count > 1 }
                        .keys

                if (duplicateIds.isNotEmpty()) {
                    duplicateIds
                            .map { duplicateId ->
                                val duplicates = value.filter { it.id == duplicateId }
                                val message = "Episode ID [$duplicateId] is used at episodes ${duplicates.map { "[${it.slug}]" }.joinToString(" and ")}."

                                ValidationResult.Failure(message)
                            }
                            .merge()
                } else {
                    ValidationResult.Success
                }
            }
            .subscribeOn(ioScheduler)
}