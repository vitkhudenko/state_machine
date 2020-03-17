package vit.khudenko.android.fsm

import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import vit.khudenko.android.fsm.test_utils.Utils.Event
import vit.khudenko.android.fsm.test_utils.Utils.Event.*
import vit.khudenko.android.fsm.test_utils.Utils.State
import vit.khudenko.android.fsm.test_utils.Utils.State.*

class StateMachineBuilderTest {

    @Test
    fun `builder should fail if no transitions have been added`() {
        assertThrows(
            "no transitions defined, make sure to call StateMachine.Builder.addTransition()",
            StateMachineBuilderValidationException::class.java
        ) {
            StateMachine.Builder<Event, State>()
                .setInitialState(STATE_A)
                .build()
        }
    }

    @Test
    fun `builder should fail if initial state has not been set`() {
        val transition = StateMachine.Transition(EVENT_1, listOf(STATE_A, STATE_B))

        assertThrows(
            "initial state is not defined, make sure to call StateMachine.Builder.setInitialState()",
            StateMachineBuilderValidationException::class.java
        ) {
            StateMachine.Builder<Event, State>()
                .addTransition(transition)
                .build()
        }
    }

    @Test
    fun `builder should fail if transition with the same event AND start state has been already added (option 1)`() {
        val event = EVENT_1
        val state = STATE_A

        val transition1 = StateMachine.Transition(event, listOf(state, STATE_B))
        val transition2 = StateMachine.Transition(event, listOf(state, STATE_B))

        assertThrows(
            getCauseForDuplicateStartState(event, state),
            StateMachineBuilderValidationException::class.java
        ) {
            StateMachine.Builder<Event, State>()
                .addTransition(transition1)
                .addTransition(transition2)
        }
    }

    @Test
    fun `builder should fail if transition with the same event AND start state has been already added (option 2)`() {
        val event = EVENT_1
        val state = STATE_A

        val transition1 = StateMachine.Transition(event, listOf(state, STATE_B))
        val transition2 = StateMachine.Transition(event, listOf(state, STATE_B, STATE_C))

        assertThrows(
            getCauseForDuplicateStartState(event, state),
            StateMachineBuilderValidationException::class.java
        ) {
            StateMachine.Builder<Event, State>()
                .addTransition(transition1)
                .addTransition(transition2)
        }
    }

    @Test
    fun `builder should fail if transition with the same event AND start state has been already added (option 3)`() {
        val event = EVENT_1
        val state = STATE_A

        val transition1 = StateMachine.Transition(event, listOf(state, STATE_B, STATE_C))
        val transition2 = StateMachine.Transition(event, listOf(state, STATE_B))

        assertThrows(
            getCauseForDuplicateStartState(event, state),
            StateMachineBuilderValidationException::class.java
        ) {
            StateMachine.Builder<Event, State>()
                .addTransition(transition1)
                .addTransition(transition2)
        }
    }

    @Test
    fun `builder should fail if there is no transition defined with start state matching the initial state`() {
        val transition = StateMachine.Transition(EVENT_1, listOf(STATE_A, STATE_B))
        val initialState = STATE_C

        assertThrows(
            "no transition defined with start state matching the initial state ($initialState)",
            StateMachineBuilderValidationException::class.java
        ) {
            StateMachine.Builder<Event, State>()
                .setInitialState(initialState)
                .addTransition(transition)
                .build()
        }
    }


    @Test
    fun `builder should successfully create state machine`() {
        val transition1 = StateMachine.Transition(EVENT_1, listOf(STATE_A, STATE_B))
        val transition2 = StateMachine.Transition(EVENT_2, listOf(STATE_B, STATE_C))
        val transition3 = StateMachine.Transition(EVENT_3, listOf(STATE_A, STATE_B, STATE_A))

        val state = STATE_A

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(transition1)
            .addTransition(transition2)
            .addTransition(transition3)
            .setInitialState(state)
            .build()

        assertSame(state, stateMachine.getCurrentState())
    }

    private fun getCauseForDuplicateStartState(event: Event, state: State) =
        "duplicate transition: a transition for event '$event' and starting state '$state' is already present"
}
