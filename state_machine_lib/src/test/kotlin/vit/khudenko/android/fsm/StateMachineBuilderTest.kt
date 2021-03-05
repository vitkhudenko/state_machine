package vit.khudenko.android.fsm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import vit.khudenko.android.fsm.test_utils.Utils.Event
import vit.khudenko.android.fsm.test_utils.Utils.Event.EVENT_1
import vit.khudenko.android.fsm.test_utils.Utils.Event.EVENT_2
import vit.khudenko.android.fsm.test_utils.Utils.Event.EVENT_3
import vit.khudenko.android.fsm.test_utils.Utils.State
import vit.khudenko.android.fsm.test_utils.Utils.State.STATE_A
import vit.khudenko.android.fsm.test_utils.Utils.State.STATE_B
import vit.khudenko.android.fsm.test_utils.Utils.State.STATE_C
import vit.khudenko.android.fsm.test_utils.assertThrows

class StateMachineBuilderTest {

    @Test
    fun `builder should fail if no transitions have been added`() {
        assertThrows(
            StateMachineBuilderValidationException::class.java,
            "no transitions defined, make sure to call StateMachine.Builder.addTransition()"
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
            StateMachineBuilderValidationException::class.java,
            "initial state is not defined, make sure to call StateMachine.Builder.setInitialState()"
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
            StateMachineBuilderValidationException::class.java,
            getCauseForDuplicateStartState(event, state)
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
            StateMachineBuilderValidationException::class.java,
            getCauseForDuplicateStartState(event, state)
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
            StateMachineBuilderValidationException::class.java,
            getCauseForDuplicateStartState(event, state)
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
            StateMachineBuilderValidationException::class.java,
            "no transition defined with start state matching the initial state ($initialState)"
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

    @Test
    fun `once a state machine is created adding a new transition to builder should not affect the state machine`() {
        val builder = StateMachine.Builder<Event, State>()
        val stateMachine = builder
            .addTransition(StateMachine.Transition(EVENT_1, listOf(STATE_A, STATE_B)))
            .setInitialState(STATE_A)
            .build()

        assertTrue(stateMachine.consumeEvent(EVENT_1))
        assertSame(STATE_B, stateMachine.getCurrentState())

        assertFalse(stateMachine.consumeEvent(EVENT_2))
        assertSame(STATE_B, stateMachine.getCurrentState())

        builder.addTransition(StateMachine.Transition(EVENT_2, listOf(STATE_B, STATE_C)))

        assertFalse(stateMachine.consumeEvent(EVENT_2))
        assertSame(STATE_B, stateMachine.getCurrentState())
    }

    private fun getCauseForDuplicateStartState(event: Event, state: State) =
        "duplicate transition: a transition for event '$event' and starting state '$state' is already present"
}
