package vit.khudenko.android.fsm

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import vit.khudenko.android.fsm.test_utils.Utils.Event.EVENT_1
import vit.khudenko.android.fsm.test_utils.Utils.State
import vit.khudenko.android.fsm.test_utils.Utils.State.*

class StateMachineTransitionTest {

    @get:Rule
    val expectedExceptionRule: ExpectedException = ExpectedException.none()

    @Test
    fun `construction should succeed if statePath has 2+ unique items`() {
        listOf(
            StateMachine.Transition(
                event = EVENT_1,
                statePath = listOf(STATE_A, STATE_B)
            ),
            StateMachine.Transition(
                event = EVENT_1,
                statePath = listOf(STATE_A, STATE_B, STATE_C)
            ),
            StateMachine.Transition(
                event = EVENT_1,
                statePath = listOf(STATE_A, STATE_B, STATE_C, STATE_D)
            ),
            StateMachine.Transition(
                event = EVENT_1,
                statePath = listOf(STATE_A, STATE_B, STATE_C, STATE_D, STATE_E)
            )
        ).forEach { transition ->
            assertEquals(
                StateMachine.Transition.Identity(transition.event, transition.statePath.first()),
                transition.identity
            )
        }
    }

    @Test
    fun `construction should fail if statePath is empty`() {
        with(expectedExceptionRule) {
            expect(IllegalArgumentException::class.java)
            expectMessage("statePath must contain at least 2 items")
        }

        StateMachine.Transition(
            event = EVENT_1,
            statePath = emptyList<State>()
        )
    }

    @Test
    fun `construction should fail if statePath has one item`() {
        with(expectedExceptionRule) {
            expect(IllegalArgumentException::class.java)
            expectMessage("statePath must contain at least 2 items")
        }

        StateMachine.Transition(
            event = EVENT_1,
            statePath = listOf(STATE_A)
        )
    }

    @Test
    fun `construction should succeed if statePath has non unique items - option 1`() {
        with(expectedExceptionRule) {
            expect(IllegalArgumentException::class.java)
            expectMessage("statePath must consist of unique items")
        }

        StateMachine.Transition(
            event = EVENT_1,
            statePath = listOf(STATE_A, STATE_A)
        )
    }

    @Test
    fun `construction should succeed if statePath has non unique items - option 2`() {
        with(expectedExceptionRule) {
            expect(IllegalArgumentException::class.java)
            expectMessage("statePath must consist of unique items")
        }

        StateMachine.Transition(
            event = EVENT_1,
            statePath = listOf(STATE_A, STATE_B, STATE_A)
        )
    }

    @Test
    fun `construction should succeed if statePath has non unique items - option 3`() {
        with(expectedExceptionRule) {
            expect(IllegalArgumentException::class.java)
            expectMessage("statePath must consist of unique items")
        }

        StateMachine.Transition(
            event = EVENT_1,
            statePath = listOf(STATE_B, STATE_A, STATE_A)
        )
    }
}