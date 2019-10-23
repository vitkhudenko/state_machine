package vit.khudenko.android.fsm

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.Mockito.*
import vit.khudenko.android.fsm.test_utils.Utils
import vit.khudenko.android.fsm.test_utils.Utils.Event
import vit.khudenko.android.fsm.test_utils.Utils.Event.*
import vit.khudenko.android.fsm.test_utils.Utils.State
import vit.khudenko.android.fsm.test_utils.Utils.State.*

class StateMachineTest {

    @get:Rule
    val expectedExceptionRule: ExpectedException = ExpectedException.none()

    @Test
    fun `initial state should be set as expected`() {
        val listener: StateMachine.Listener<State> = mock()

        val state = STATE_A

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(state, STATE_B)
                )
            )
            .setInitialState(state)
            .build()

        stateMachine.addListener(listener)

        assertEquals(state, stateMachine.getCurrentState())
        verify(listener, never()).onStateChanged(anyOrNull(), anyOrNull())
    }

    @Test
    fun `out-of-config events should be ignored`() {
        val listener: StateMachine.Listener<State> = mock()

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_2,
                    listOf(STATE_B, STATE_C)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_3,
                    listOf(STATE_C, STATE_D, STATE_E)
                )
            )
            .setInitialState(STATE_A)
            .build()

        stateMachine.addListener(listener)

        Utils.checkEventsAreIgnored(
            stateMachine,
            listener,
            listOf(EVENT_4, EVENT_5)
        )
    }

    @Test
    fun `in-config events, that do not match the current state of the state machine, should be ignored`() {
        val listener: StateMachine.Listener<State> = mock()

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_2,
                    listOf(STATE_B, STATE_C)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_3,
                    listOf(STATE_C, STATE_D, STATE_E)
                )
            )
            .setInitialState(STATE_A)
            .build()

        stateMachine.addListener(listener)

        Utils.checkEventsAreIgnored(
            stateMachine,
            listener,
            listOf(EVENT_2, EVENT_3)
        )
    }

    @Test
    fun `same transition should not happen, if the same event is fired again`() {
        val listener: StateMachine.Listener<State> = mock()

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_2,
                    listOf(STATE_B, STATE_C)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_3,
                    listOf(STATE_C, STATE_D, STATE_E)
                )
            )
            .setInitialState(STATE_A)
            .build()

        stateMachine.addListener(listener)

        // verify transition from STATE_A to STATE_B happens
        Utils.checkEventIsConsumed(
            stateMachine,
            listOf(listener),
            EVENT_1,
            listOf(STATE_A, STATE_B)
        )

        Utils.checkEventIsIgnored(
            stateMachine,
            listener,
            EVENT_1
        )
    }

    @Test
    fun `several transitions and final state`() {
        val listener: StateMachine.Listener<State> = mock()

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_2,
                    listOf(STATE_B, STATE_C)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_3,
                    listOf(STATE_C, STATE_D, STATE_E)
                )
            )
            .setInitialState(STATE_A)
            .build()

        stateMachine.addListener(listener)

        assertEquals(STATE_A, stateMachine.getCurrentState())
        verify(listener, never()).onStateChanged(anyOrNull(), anyOrNull())

        // verify transition from STATE_A to STATE_B happens
        Utils.checkEventIsConsumed(
            stateMachine,
            listOf(listener),
            EVENT_1,
            listOf(STATE_A, STATE_B)
        )

        // verify transition from STATE_B to STATE_C happens
        Utils.checkEventIsConsumed(
            stateMachine,
            listOf(listener),
            EVENT_2,
            listOf(STATE_B, STATE_C)
        )

        // verify transition from STATE_C to STATE_D to STATE_E happens
        Utils.checkEventIsConsumed(
            stateMachine,
            listOf(listener),
            EVENT_3,
            listOf(STATE_C, STATE_D, STATE_E)
        )

        // verify any further events should are ignored as state machine is its final state
        Utils.checkEventsAreIgnored(
            stateMachine,
            listener,
            Event.values().asList()
        )
    }

    @Test
    fun `starting new transition while ongoing transition is not finished yet should be a consistency violation`() {
        expectedExceptionRule.expect(IllegalStateException::class.java)
        expectedExceptionRule.expectMessage("there is a transition which is still in progress")

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B, STATE_C)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_2,
                    listOf(STATE_B, STATE_D)
                )
            )
            .setInitialState(STATE_A)
            .build()

        val listener: StateMachine.Listener<State> = mock {
            // listener will fire EVENT_2 as soon as the intermediate state STATE_B
            // in the first transition (STATE_A - STATE_B - STATE_C) is reached
            on { onStateChanged(STATE_A, STATE_B) } doAnswer {
                assertEquals(STATE_B, stateMachine.getCurrentState())
                assertTrue(stateMachine.consumeEvent(EVENT_2))
                Unit
            }
        }

        stateMachine.addListener(listener)

        // The EVENT_2, triggered from the listener.onStateChanged(STATE_A, STATE_B),
        // breaks state machine consistency, so it should crash. Otherwise the second transition (STATE_B to STATE_D)
        // would start while the first transition is still in the intermediate state STATE_B.
        stateMachine.consumeEvent(EVENT_1)
    }

    @Test
    fun `starting new transition once ongoing transition has finished should not be a consistency violation (case with 2 transitions)`() {
        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B, STATE_C)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_2,
                    listOf(STATE_C, STATE_D, STATE_E)
                )
            )
            .setInitialState(STATE_A)
            .build()

        val listener: StateMachine.Listener<State> = mock {
            // listener will fire EVENT_2 as soon as the final state STATE_C in the first
            // transition (STATE_A - STATE_B - STATE_C) is reached
            on { onStateChanged(STATE_B, STATE_C) } doAnswer {
                assertEquals(STATE_C, stateMachine.getCurrentState())
                assertTrue(stateMachine.consumeEvent(EVENT_2))
                Unit
            }
        }
        stateMachine.addListener(listener)

        // verify that EVENT_2, triggered from the listener.onStateChanged(STATE_B, STATE_C),
        // does not break state machine consistency (otherwise it would crash)
        Utils.checkEventIsConsumed(
            stateMachine,
            listOf(listener),
            EVENT_1,
            listOf(STATE_A, STATE_B, STATE_C, STATE_D, STATE_E)
        )
    }

    @Test
    fun `starting new transition once ongoing transition has finished should not be a consistency violation (case with 3 transitions)`() {
        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_2,
                    listOf(STATE_B, STATE_C)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_3,
                    listOf(STATE_C, STATE_D)
                )
            )
            .setInitialState(STATE_A)
            .build()

        val listener: StateMachine.Listener<State> = mock {
            // listener will fire EVENT_2 as soon as the final state STATE_B in the first
            // transition (STATE_A - STATE_B) is reached
            on { onStateChanged(STATE_A, STATE_B) } doAnswer {
                assertEquals(STATE_B, stateMachine.getCurrentState())
                assertTrue(stateMachine.consumeEvent(EVENT_2))
                Unit
            }
            // listener will fire EVENT_3 as soon as the final state STATE_C in the second
            // transition (STATE_B - STATE_C) is reached
            on { onStateChanged(STATE_B, STATE_C) } doAnswer {
                assertEquals(STATE_C, stateMachine.getCurrentState())
                assertTrue(stateMachine.consumeEvent(EVENT_3))
                Unit
            }
        }
        stateMachine.addListener(listener)

        // Verify that:
        // a) EVENT_2, fired from the listener.onStateChanged(STATE_A, STATE_B),
        //    does not break state machine consistency.
        // b) EVENT_3, fired from the listener.onStateChanged(STATE_B, STATE_C),
        //    does not break state machine consistency.
        Utils.checkEventIsConsumed(
            stateMachine,
            listOf(listener),
            EVENT_1,
            listOf(STATE_A, STATE_B, STATE_C, STATE_D)
        )
    }

    @Test
    fun `both listeners should be notified as expected`() {
        val listener1: StateMachine.Listener<State> = mock()
        val listener2: StateMachine.Listener<State> = mock()

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B)
                )
            )
            .setInitialState(STATE_A)
            .build()

        with(stateMachine) {
            addListener(listener1)
            addListener(listener2)
        }

        Utils.checkEventIsConsumed(
            stateMachine,
            listOf(listener1, listener2),
            EVENT_1,
            listOf(STATE_A, STATE_B)
        )
    }

    @Test
    fun `adding same listener twice should be a no op`() {
        val listener: StateMachine.Listener<State> = mock()

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B)
                )
            )
            .setInitialState(STATE_A)
            .build()

        with(stateMachine) {
            addListener(listener)
            addListener(listener)
        }

        Utils.checkEventIsConsumed(
            stateMachine,
            listOf(listener),
            EVENT_1,
            listOf(STATE_A, STATE_B)
        )
    }

    @Test
    fun `explicit call to remove one of the two listeners`() {
        val listener1: StateMachine.Listener<State> = mock()
        val listener2: StateMachine.Listener<State> = mock()

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_2,
                    listOf(STATE_B, STATE_C)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_3,
                    listOf(STATE_C, STATE_D)
                )
            )
            .setInitialState(STATE_A)
            .build()


        with(stateMachine) {
            addListener(listener1)
            addListener(listener2)
        }

        Utils.checkEventIsConsumed(
            stateMachine,
            listOf(listener1, listener2),
            EVENT_1,
            listOf(STATE_A, STATE_B)
        )

        // leave only listener2 attached to state machine
        stateMachine.removeListener(listener1)

        assertTrue(stateMachine.consumeEvent(EVENT_2))
        verify(listener2).onStateChanged(STATE_B, STATE_C)
        verifyNoMoreInteractions(listener1, listener2)

        // leave no listeners attached to state machine
        stateMachine.removeListener(listener2)

        assertEquals(STATE_C, stateMachine.getCurrentState())
        assertTrue(stateMachine.consumeEvent(EVENT_3))
        assertEquals(STATE_D, stateMachine.getCurrentState())
        verifyNoMoreInteractions(listener1, listener2)
    }

    @Test
    fun `first listener removes itself during notification`() {
        val listener1: StateMachine.Listener<State> = mock()
        val listener2: StateMachine.Listener<State> = mock()

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_2,
                    listOf(STATE_B, STATE_C)
                )
            )
            .setInitialState(STATE_A)
            .build()


        doAnswer {
            // listener1 will remove itself as soon as notified
            stateMachine.removeListener(listener1)
        }.`when`(listener1).onStateChanged(STATE_A, STATE_B)

        with(stateMachine) {
            addListener(listener1)
            addListener(listener2)
        }

        // verify state changed and both listeners are notified as expected
        Utils.checkEventIsConsumed(
            stateMachine,
            listOf(listener1, listener2),
            EVENT_1,
            listOf(STATE_A, STATE_B)
        )

        // verify state changed and only remaining listener2 is notified
        assertTrue(stateMachine.consumeEvent(EVENT_2))
        verify(listener2).onStateChanged(STATE_B, STATE_C)
        verifyNoMoreInteractions(listener1, listener2)
    }

    @Test
    fun `second listener removes itself during notification`() {
        val listener1: StateMachine.Listener<State> = mock()
        val listener2: StateMachine.Listener<State> = mock()

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_2,
                    listOf(STATE_B, STATE_C)
                )
            )
            .setInitialState(STATE_A)
            .build()

        doAnswer {
            // listener2 will remove itself as soon as notified
            stateMachine.removeListener(listener2)
        }.`when`(listener1).onStateChanged(STATE_A, STATE_B)

        with(stateMachine) {
            addListener(listener1)
            addListener(listener2)
        }

        // verify state changed and both listeners are notified as expected
        Utils.checkEventIsConsumed(
            stateMachine,
            listOf(listener1, listener2),
            EVENT_1,
            listOf(STATE_A, STATE_B)
        )

        // verify state changed and only remaining listener1 is notified
        assertTrue(stateMachine.consumeEvent(EVENT_2))
        verify(listener1).onStateChanged(STATE_B, STATE_C)
        verifyNoMoreInteractions(listener1, listener2)
    }

    @Test
    fun `first listener removes second listener during notification`() {
        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_2,
                    listOf(STATE_B, STATE_C)
                )
            )
            .setInitialState(STATE_A)
            .build()

        val listener2: StateMachine.Listener<State> = mock()
        val listener1: StateMachine.Listener<State> = mock {
            on {
                // listener1 will remove listener2 as soon as notified
                onStateChanged(STATE_A, STATE_B)
            } doAnswer { stateMachine.removeListener(listener2) }
        }

        with(stateMachine) {
            addListener(listener1)
            addListener(listener2)
        }

        // verify state changed and both listeners are notified as expected
        Utils.checkEventIsConsumed(
            stateMachine,
            listOf(listener1, listener2),
            EVENT_1,
            listOf(STATE_A, STATE_B)
        )

        // verify state changed and only remaining listener1 is notified
        assertTrue(stateMachine.consumeEvent(EVENT_2))
        verify(listener1).onStateChanged(STATE_B, STATE_C)
        verifyNoMoreInteractions(listener1, listener2)
    }

    @Test
    fun `second listener removes first listener during notification`() {
        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B)
                )
            )
            .addTransition(
                StateMachine.Transition(
                    EVENT_2,
                    listOf(STATE_B, STATE_C)
                )
            )
            .setInitialState(STATE_A)
            .build()

        val listener1: StateMachine.Listener<State> = mock()
        val listener2: StateMachine.Listener<State> = mock {
            on {
                // listener2 will remove listener1 as soon as notified
                onStateChanged(STATE_A, STATE_B)
            } doAnswer { stateMachine.removeListener(listener1) }
        }

        with(stateMachine) {
            addListener(listener1)
            addListener(listener2)
        }

        // verify state changed and both listeners are notified as expected
        Utils.checkEventIsConsumed(
            stateMachine,
            listOf(listener1, listener2),
            EVENT_1,
            listOf(STATE_A, STATE_B)
        )

        // verify state changed and only remaining listener2 is notified as expected
        assertTrue(stateMachine.consumeEvent(EVENT_2))
        verify(listener2).onStateChanged(STATE_B, STATE_C)
        verifyNoMoreInteractions(listener1, listener2)
    }

    @Test
    fun `remove all listeners via removeAllListeners()`() {
        val listener1: StateMachine.Listener<State> = mock()
        val listener2: StateMachine.Listener<State> = mock()

        val stateMachine = StateMachine.Builder<Event, State>()
            .addTransition(
                StateMachine.Transition(
                    EVENT_1,
                    listOf(STATE_A, STATE_B)
                )
            )
            .setInitialState(STATE_A)
            .build()

        with(stateMachine) {
            addListener(listener1)
            addListener(listener2)
            removeAllListeners()
        }

        assertEquals(STATE_A, stateMachine.getCurrentState())

        assertTrue(stateMachine.consumeEvent(EVENT_1))

        assertEquals(STATE_B, stateMachine.getCurrentState())

        verifyZeroInteractions(listener1, listener2)
    }
}