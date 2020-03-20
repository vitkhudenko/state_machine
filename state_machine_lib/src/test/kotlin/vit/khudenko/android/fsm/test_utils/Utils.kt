package vit.khudenko.android.fsm.test_utils

import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import vit.khudenko.android.fsm.StateMachine
import java.util.Optional

class Utils {

    companion object {

        fun checkEventIsIgnored(
            stateMachine: StateMachine<Event, State, String>,
            listener: StateMachine.Listener<State, String>,
            eventItem: Pair<Event, Optional<String>>
        ) {
            checkEventsAreIgnored(
                stateMachine,
                listOf(listener),
                listOf(eventItem)
            )
        }

        fun checkEventsAreIgnored(
            stateMachine: StateMachine<Event, State, String>,
            listener: StateMachine.Listener<State, String>,
            eventItems: List<Pair<Event, Optional<String>>>
        ) {
            checkEventsAreIgnored(
                stateMachine,
                listOf(listener),
                eventItems
            )
        }

        fun checkEventIsConsumed(
            stateMachine: StateMachine<Event, State, String>,
            listeners: List<StateMachine.Listener<State, String>>,
            eventItem: Pair<Event, Optional<String>>,
            transition: List<State>
        ) {
            val (event, eventPayloadOptional) = eventItem

            assertEquals(transition.first(), stateMachine.getCurrentState())
            assertTrue(stateMachine.consumeEvent(event, eventPayloadOptional.getOrNull()))
            assertEquals(transition.last(), stateMachine.getCurrentState())

            for (listener in listeners) {
                with(inOrder(listener)) {
                    transition.windowed(size = 2, step = 1, partialWindows = false)
                        .map { window ->
                            window.first() to window.last()
                        }
                        .forEach { (stateFrom, stateTo) ->
                            verify(listener).onStateChanged(stateFrom, stateTo, eventPayloadOptional.getOrNull())
                        }
                }
                verifyNoMoreInteractions(listener)
            }
        }

        fun eventItem(event: Event, eventPayload: String? = null) = Pair(event, Optional.ofNullable(eventPayload))

        private fun checkEventsAreIgnored(
            stateMachine: StateMachine<Event, State, String>,
            listeners: List<StateMachine.Listener<State, String>>,
            eventItems: List<Pair<Event, Optional<String>>>
        ) {
            val state = stateMachine.getCurrentState()
            val listenersCopy = ArrayList(listeners)
            for ((event, eventPayloadOptional) in eventItems) {
                assertFalse(stateMachine.consumeEvent(event, eventPayloadOptional.getOrNull()))
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

fun <T> Optional<T>.getOrNull(): T? = if (this.isPresent) {
    get()
} else {
    null
}
