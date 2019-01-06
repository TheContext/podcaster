package io.thecontext.podcaster.validation

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.context
import com.greghaskins.spectrum.dsl.specification.Specification.it
import io.reactivex.schedulers.Schedulers
import io.thecontext.podcaster.memoized
import io.thecontext.podcaster.testEpisode
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class EpisodesValidatorSpec {
    init {
        val validator by memoized { EpisodesValidator(Schedulers.trampoline()) }

        context("episodes with unique IDs") {

            val episodes = listOf(
                    testEpisode.copy(id = "id1"),
                    testEpisode.copy(id = "id2")
            )

            it("returns validation result successful") {
                validator.validate(episodes)
                        .test()
                        .assertResult(ValidationResult.Success)
            }
        }

        context("episodes with duplicating IDs") {

            val episodes = listOf(
                    testEpisode.copy(id = "id1"),
                    testEpisode.copy(id = "id2"),
                    testEpisode.copy(id = "id2")
            )

            it("returns validation result failure") {
                validator.validate(episodes)
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }
    }
}