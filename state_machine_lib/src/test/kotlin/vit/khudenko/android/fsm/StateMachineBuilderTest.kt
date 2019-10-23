package vit.khudenko.android.fsm

import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import vit.khudenko.android.fsm.test_utils.Utils.Event
import vit.khudenko.android.fsm.test_utils.Utils.Event.EVENT_1
import vit.khudenko.android.fsm.test_utils.Utils.Event.EVENT_2
import vit.khudenko.android.fsm.test_utils.Utils.State
import vit.khudenko.android.fsm.test_utils.Utils.State.*

class StateMachineBuilderTest {

    @get:Rule
    val expectedExceptionRule: ExpectedException = ExpectedException.none()

    @Test
    fun `builder should fail if no transitions have been added`() {
        with(expectedExceptionRule) {
            expect(StateMachineBuilderValidationException::class.java)
            expectMessage("no transitions defined, make sure to call StateMachine.Builder.addTransition()")
        }

        StateMachine.Builder<Event, State>()
            .setInitialState(STATE_A)
            .build()
    }

    @Test
    fun `builder should fail if initial state has not been set`() {
        val transition = StateMachine.Transition(EVENT_1, listOf(STATE_A, STATE_B))

        with(expectedExceptionRule) {
            expect(StateMachineBuilderValidationException::class.java)
            expectMessage("initial state is not defined, make sure to call StateMachine.Builder.setInitialState()")
        }

        StateMachine.Builder<Event, State>()
            .addTransition(transition)
            .build()
    }

    @Test
    fun `builder should fail if transition with the same event AND start state has been already added (option 1)`() {
        val event = EVENT_1
        val state = STATE_A

        val transition1 = StateMachine.Transition(event, listOf(state, STATE_B))
        val transition2 = StateMachine.Transition(event, listOf(state, STATE_B))

        with(expectedExceptionRule) {
            expect(StateMachineBuilderValidationException::class.java)
            expectMessage(getCauseForDuplicateStartState(event, state))
        }

        StateMachine.Builder<Event, State>()
            .addTransition(transition1)
            .addTransition(transition2)
    }

    @Test
    fun `builder should fail if transition with the same event AND start state has been already added (option 2)`() {
        val event = EVENT_1
        val state = STATE_A

        val transition1 = StateMachine.Transition(event, listOf(state, STATE_B))
        val transition2 = StateMachine.Transition(event, listOf(state, STATE_B, STATE_C))

        with(expectedExceptionRule) {
            expect(StateMachineBuilderValidationException::class.java)
            expectMessage(getCauseForDuplicateStartState(event, state))
        }

        StateMachine.Builder<Event, State>()
            .addTransition(transition1)
            .addTransition(transition2)
    }

    @Test
    fun `builder should fail if transition with the same event AND start state has been already added (option 3)`() {
        val event = EVENT_1
        val state = STATE_A

        val transition1 = StateMachine.Transition(event, listOf(state, STATE_B, STATE_C))
        val transition2 = StateMachine.Transition(event, listOf(state, STATE_B))

        with(expectedExceptionRule) {
            expect(StateMachineBuilderValidationException::class.java)
            expectMessage(getCauseForDuplicateStartState(event, state))
        }

        StateMachine.Builder<Event, State>()
            .addTransition(transition1)
            .addTransition(transition2)
    }

    @Test
    fun `builder should successfully create state machine`() {
        val transition1 = StateMachine.Transition(EVENT_1, listOf(STATE_A, STATE_B))
        val transition2 = StateMachine.Transition(EVENT_2, listOf(STATE_B, STATE_C))

        val state = STATE_A

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(transition1)
            .addTransition(transition2)
            .setInitialState(state)
            .build()

        assertSame(state, stateMachine.getCurrentState())
    }

    private fun getCauseForDuplicateStartState(event: Event, state: State) =
        "duplicate transition: a transition for event '$event' and starting state '$state' is already present"
}
