package vit.khudenko.android.fsm

import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import vit.khudenko.android.fsm.test_utils.Utils.Event
import vit.khudenko.android.fsm.test_utils.Utils.Event.EVENT_1
import vit.khudenko.android.fsm.test_utils.Utils.State
import vit.khudenko.android.fsm.test_utils.Utils.State.STATE_A
import vit.khudenko.android.fsm.test_utils.Utils.State.STATE_B
import vit.khudenko.android.fsm.test_utils.Utils.State.STATE_C

class StateMachineBuilderTest {

    @get:Rule
    val expectedExceptionRule: ExpectedException = ExpectedException.none()

    @Test
    fun `builder should fail if no transitions have been added`() {
        with(expectedExceptionRule) {
            expect(StateMachineBuilderValidationException::class.java)
            expectMessage("at least one transition must be added")
        }

        StateMachine.Builder<Event, State>()
            .setInitialState(STATE_A)
            .build()
    }

    @Test
    fun `builder should fail if initial state has not been set`() {
        with(expectedExceptionRule) {
            expect(StateMachineBuilderValidationException::class.java)
            expectMessage("initialState must be set")
        }

        StateMachine.Builder<Event, State>()
            .addTransition(
                EVENT_1,
                listOf(STATE_A, STATE_B)
            )
            .build()
    }

    @Test
    fun `builder should fail if states are not unique within a transition (option 1)`() {
        with(expectedExceptionRule) {
            expect(StateMachineBuilderValidationException::class.java)
            expectMessage("statePath must consist of unique states")
        }

        StateMachine.Builder<Event, State>()
            .addTransition(
                EVENT_1,
                listOf(STATE_A, STATE_A)
            )
    }

    @Test
    fun `builder should fail if states are not unique within a transition (option 2)`() {
        with(expectedExceptionRule) {
            expect(StateMachineBuilderValidationException::class.java)
            expectMessage("statePath must consist of unique states")
        }

        StateMachine.Builder<Event, State>()
            .addTransition(
                EVENT_1,
                listOf(STATE_A, STATE_B, STATE_C, STATE_B)
            )
    }

    @Test
    fun `builder should fail if a transition has no states`() {
        with(expectedExceptionRule) {
            expect(StateMachineBuilderValidationException::class.java)
            expectMessage("statePath must have at least 2 states")
        }

        StateMachine.Builder<Event, State>()
            .addTransition(
                EVENT_1,
                ArrayList()
            )
    }

    @Test
    fun `builder should fail if a transition has only one state`() {
        with(expectedExceptionRule) {
            expect(StateMachineBuilderValidationException::class.java)
            expectMessage("statePath must have at least 2 states")
        }

        StateMachine.Builder<Event, State>()
            .addTransition(
                EVENT_1,
                listOf(STATE_A)
            )
    }

    @Test
    fun `builder should fail if transition with the same event AND start state has been already added (option 1)`() {
        val event = EVENT_1
        val state = STATE_A

        with(expectedExceptionRule) {
            expect(StateMachineBuilderValidationException::class.java)
            expectMessage(getCauseForDuplicateStartState(state, event))
        }

        StateMachine.Builder<Event, State>()
            .addTransition(
                event,
                listOf(state, STATE_B)
            )
            .addTransition(
                event,
                listOf(state, STATE_B)
            )
    }

    @Test
    fun `builder should fail if transition with the same event AND start state has been already added (option 2)`() {
        val event = EVENT_1
        val state = STATE_A

        with(expectedExceptionRule) {
            expect(StateMachineBuilderValidationException::class.java)
            expectMessage(getCauseForDuplicateStartState(state, event))
        }

        StateMachine.Builder<Event, State>()
            .addTransition(
                event,
                listOf(state, STATE_B)
            )
            .addTransition(
                event,
                listOf(state, STATE_B, STATE_C)
            )
    }

    @Test
    fun `builder should fail if transition with the same event AND start state has been already added (option 3)`() {
        val event = EVENT_1
        val state = STATE_A

        with(expectedExceptionRule) {
            expect(StateMachineBuilderValidationException::class.java)
            expectMessage(getCauseForDuplicateStartState(state, event))
        }

        StateMachine.Builder<Event, State>()
            .addTransition(
                event,
                listOf(state, STATE_B, STATE_C)
            )
            .addTransition(
                event,
                listOf(state, STATE_B)
            )
    }

    @Test
    fun `builder should successfully create state machine`() {
        val state = STATE_A

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                EVENT_1,
                listOf(STATE_A, STATE_B)
            )
            .addTransition(
                Event.EVENT_2,
                listOf(STATE_B, STATE_C)
            )
            .setInitialState(state)
            .build()

        assertSame(state, stateMachine.getCurrentState())
    }

    private fun getCauseForDuplicateStartState(state: State, event: Event) =
        "statePath with the same start state $state is already defined for the event $event"
}
