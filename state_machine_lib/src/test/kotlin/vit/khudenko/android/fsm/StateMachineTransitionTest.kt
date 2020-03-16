package vit.khudenko.android.fsm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import vit.khudenko.android.fsm.test_utils.Utils.Event.EVENT_1
import vit.khudenko.android.fsm.test_utils.Utils.State
import vit.khudenko.android.fsm.test_utils.Utils.State.*
import java.util.Optional

@RunWith(Parameterized::class)
class StateMachineTransitionTest(
    private val statePath: List<State>,
    private val expectedExceptionOptional: Optional<Exception>
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0} -> \"{1}\"")
        fun data(): List<Array<Any>> = listOf(
            arrayOf(
                listOf(STATE_A, STATE_B),
                Optional.empty<Exception>()
            ),
            arrayOf(
                listOf(STATE_A, STATE_B, STATE_C),
                Optional.empty<Exception>()
            ),
            arrayOf(
                listOf(STATE_A, STATE_B, STATE_A),
                Optional.empty<Exception>()
            ),
            arrayOf(
                listOf(STATE_A, STATE_B, STATE_A, STATE_B),
                Optional.empty<Exception>()
            ),
            arrayOf(
                listOf(STATE_A, STATE_B, STATE_C, STATE_D),
                Optional.empty<Exception>()
            ),
            arrayOf(
                listOf(STATE_A, STATE_B, STATE_C, STATE_D, STATE_E),
                Optional.empty<Exception>()
            ),
            arrayOf(
                emptyList<State>(),
                Optional.of(IllegalArgumentException("statePath must contain at least 2 items"))
            ),
            arrayOf(
                listOf(STATE_A),
                Optional.of(IllegalArgumentException("statePath must contain at least 2 items"))
            ),
            arrayOf(
                listOf(STATE_A, STATE_A),
                Optional.of(IllegalArgumentException("statePath must not have repeating items in a row"))
            ),
            arrayOf(
                listOf(STATE_A, STATE_A, STATE_B),
                Optional.of(IllegalArgumentException("statePath must not have repeating items in a row"))
            ),
            arrayOf(
                listOf(STATE_A, STATE_B, STATE_B),
                Optional.of(IllegalArgumentException("statePath must not have repeating items in a row"))
            ),
            arrayOf(
                listOf(STATE_A, STATE_B, STATE_B, STATE_A),
                Optional.of(IllegalArgumentException("statePath must not have repeating items in a row"))
            )
        )
    }

    @Test
    fun `test construction`() {
        if (expectedExceptionOptional.isPresent) {
            expectedExceptionOptional.get().also { exception ->
                assertThrows(exception.message, exception.javaClass) { StateMachine.Transition(EVENT_1, statePath) }
            }
        } else {
            StateMachine.Transition(EVENT_1, statePath).also { transition ->
                assertEquals(
                    StateMachine.Transition.Identity(transition.event, transition.statePath.first()),
                    transition.identity
                )
            }
        }
    }
}