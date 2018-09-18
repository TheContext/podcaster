package io.thecontext.ci.validation

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.*
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.memoized
import io.thecontext.ci.testEpisode
import io.thecontext.ci.value.Episode
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class EpisodeListValidatorSpec {
    init {
        val validator by memoized { EpisodeListValidator(Schedulers.trampoline()) }

        context("list of episode with unique ids") {
            val episodes = listOf(
                    testEpisode.copy(
                            id = "id1",
                            title = "Episode 1"
                    ),
                    testEpisode.copy(
                            id = "id2",
                            title = "Episode 2"
                    )
            )
            it("returns validation result successful") {
                validator.validate(episodes)
                        .test()
                        .assertValue { it is ValidationResult.Success }
            }
        }

        context("list of episode with same ids") {
            val episodes = listOf(
                    testEpisode.copy(
                            id = "someID",
                            title = "Episode 1"
                    ),
                    testEpisode.copy(
                            id = "someID",
                            title = "Episode 2"
                    ),
                    testEpisode,
                    testEpisode.copy(
                            id = "someID2",
                            title = "Episode 3"
                    ),
                    testEpisode.copy(
                            id = "someID2",
                            title = "Episode 4"
                    )
            )
            it("returns validation result failure") {
                validator.validate(episodes)
                        .test()
                        .assertValue {
                            it is ValidationResult.Failure
                                    && it.message == "Error: Episode id must be unique but id = \"someID\" is used for \"Episode 2\" and \"Episode 1\"\n" +
                                    "Error: Episode id must be unique but id = \"someID2\" is used for \"Episode 4\" and \"Episode 3\""
                        }
            }
        }
    }
}