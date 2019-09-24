package vit.khudenko.android.fsm.test_utils

import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import vit.khudenko.android.fsm.StateMachine

class Utils {

    companion object {

        fun checkEventIsIgnored(
            stateMachine: StateMachine<Event, State>,
            listener: StateMachine.Listener<State>,
            event: Event
        ) {
            checkEventsAreIgnored(
                stateMachine,
                listOf(listener),
                listOf(event)
            )
        }

        fun checkEventsAreIgnored(
            stateMachine: StateMachine<Event, State>,
            listener: StateMachine.Listener<State>,
            events: List<Event>
        ) {
            checkEventsAreIgnored(
                stateMachine,
                listOf(listener),
                events
            )
        }

        fun checkEventIsConsumed(
            stateMachine: StateMachine<Event, State>,
            listeners: List<StateMachine.Listener<State>>,
            event: Event,
            transition: List<State>
        ) {
            assertEquals(transition.first(), stateMachine.getCurrentState())
            assertTrue(stateMachine.consumeEvent(event))
            assertEquals(transition.last(), stateMachine.getCurrentState())

            for (listener in listeners) {
                with(inOrder(listener)) {
                    transition.windowed(size = 2, step = 1, partialWindows = false)
                        .map { window ->
                            window.first() to window.last()
                        }
                        .forEach { (stateFrom, stateTo) ->
                            verify(listener).onStateChanged(stateFrom, stateTo)
                        }
                }
                verifyNoMoreInteractions(listener)
            }
        }

        private fun checkEventsAreIgnored(
            stateMachine: StateMachine<Event, State>,
            listeners: List<StateMachine.Listener<State>>,
            events: List<Event>
        ) {
            val state = stateMachine.getCurrentState()
            val listenersCopy = ArrayList(listeners)
            for (event in events) {
                assertFalse(stateMachine.consumeEvent(event))
                assertEquals(state, stateMachine.getCurrentState())
                for (listener in listenersCopy) {
                    verifyNoMoreInteractions(listener)
                }
            }
        }
    }

    enum class State {
        STATE_A,
        STATE_B,
        STATE_C,
        STATE_D,
        STATE_E
    }

    enum class Event {
        EVENT_1,
        EVENT_2,
        EVENT_3,
        EVENT_4,
        EVENT_5
    }
}