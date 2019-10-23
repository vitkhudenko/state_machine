package vit.khudenko.android.fsm

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet
import kotlin.collections.set

/**
 * `StateMachine` is a general purpose finite-state machine.
 *
 * A sample configuration (assuming your app has `Session` class, that defines specific to your app events and states
 * (`Session.Event` and `Session.State` enums):
 *
 * ```
 *    val sessionStateMachine = StateMachine.Builder<Session.Event, Session.State>()
 *        .setInitialState(Session.State.ACTIVE)
 *        .addTransition(
 *            StateMachine.Transition(
 *                event = Session.Event.LOGIN,
 *                statePath = listOf(Session.State.INACTIVE, Session.State.ACTIVE)
 *            )
 *        )
 *        .addTransition(
 *            StateMachine.Transition(
 *                event = Session.Event.LOGOUT,
 *                statePath = listOf(Session.State.ACTIVE, Session.State.INACTIVE)
 *            )
 *        )
 *        .addTransition(
 *            StateMachine.Transition(
 *                event = Session.Event.LOGOUT_AND_FORGET,
 *                statePath = listOf(Session.State.ACTIVE, Session.State.FORGOTTEN)
 *            )
 *        )
 *        .build()
 * ```
 *
 * The implementation is thread-safe. Public API methods are declared as `synchronized`.
 *
 * @param [Event] event parameter of enum type.
 * @param [State] state parameter of enum type.
 *
 * @see [StateMachine.Builder]
 */
class StateMachine<Event : Enum<Event>, State : Enum<State>> private constructor(
    private val graph: Map<Pair<Event, State>, List<State>>,
    initialState: State
) {

    /**
     * A callback to communicate state changes of a [`StateMachine`][StateMachine].
     */
    interface Listener<State> {
        fun onStateChanged(oldState: State, newState: State)
    }

    /**
     * Builder is not thread-safe.
     */
    class Builder<Event : Enum<Event>, State : Enum<State>> {

        private val graph = HashMap<Pair<Event, State>, List<State>>()
        private lateinit var initialState: State

        /**
         * A transition defines its identity as a pair of the [`event`][event] and the starting state
         * (the first item in the [`statePath`][statePath]). `StateMachine` allows unique transitions
         * only (each transition must have a unique identity).
         *
         * @param event [`Event`][Event].
         * @param statePath a list of states.
         *
         * @return [`StateMachine.Builder`][StateMachine.Builder]
         *
         * @throws [StateMachineBuilderValidationException] if a duplicate transition identified (by a combination
         *                                                  of event and starting state)
         */
        fun addTransition(transition: Transition<Event, State>): Builder<Event, State> {
            val statePathCopy = transition.statePath.toMutableList()
            val startState = statePathCopy.removeAt(0)

            if (graph.containsKey(transition.identity)) {
                val cause = "duplicate transition: a transition for event '" + transition.event +
                        "' and starting state '" + startState + "' is already present"
                throw StateMachineBuilderValidationException(cause)
            }

            graph[transition.identity] = Collections.unmodifiableList(statePathCopy)

            return this
        }

        /**
         * @param state [`State`][State]
         *
         * @return [`StateMachine.Builder`][StateMachine.Builder]
         */
        fun setInitialState(state: State): Builder<Event, State> {
            this.initialState = state
            return this
        }

        /**
         * @return [`StateMachine`][StateMachine] a newly created instance.
         *
         * @throws [StateMachineBuilderValidationException] if initial state has not been set (see [setInitialState])
         * @throws [StateMachineBuilderValidationException] if no transitions have been added (see [addTransition])
         */
        fun build(): StateMachine<Event, State> {
            if (this::initialState.isInitialized.not()) {
                throw StateMachineBuilderValidationException(
                    "initial state is not defined, make sure to call ${StateMachine::class.java.simpleName}" +
                            ".${javaClass.simpleName}.setInitialState()"
                )
            }
            if (graph.isEmpty()) {
                throw StateMachineBuilderValidationException(
                    "no transitions defined, make sure to call ${StateMachine::class.java.simpleName}" +
                            ".${javaClass.simpleName}.addTransition()"
                )
            }
            return StateMachine(graph, initialState)
        }
    }

    private var currentState: State = initialState
    private val listeners: LinkedHashSet<Listener<State>> = LinkedHashSet()
    private var inTransition: Boolean = false

    /**
     * Adds [`listener`][listener] to this `StateMachine`.
     *
     * If this [`listener`][listener] has been already added, then this call is no op.
     */
    @Synchronized
    fun addListener(listener: Listener<State>) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    /**
     * Removes all listeners from this `StateMachine`.
     */
    @Synchronized
    fun removeAllListeners() {
        listeners.clear()
    }

    /**
     * Removes [`listener`][listener] from this `StateMachine`.
     */
    @Synchronized
    fun removeListener(listener: Listener<State>) {
        listeners.remove(listener)
    }

    /**
     * Moves the `StateMachine` from the current state to a new one (if there is a matching transition).
     *
     * Depending on configuration of this `StateMachine` there may be several state changes for one
     * [`consumeEvent()`][consumeEvent] call.
     *
     * Missed [`consumeEvent()`][consumeEvent] calls (meaning no matching transition found) are ignored (no op).
     *
     * State changes are communicated via the [`StateMachine.Listener`][StateMachine.Listener] listeners.
     *
     * @param event [`Event`][Event]
     *
     * @return flag of whether the event was actually consumed (meaning moving to a new state) or ignored.
     *
     * @throws [IllegalStateException] if there is a matching transition for this event and current state,
     * but there is still an on-going unfinished transition.
     */
    @Synchronized
    fun consumeEvent(event: Event): Boolean {
        val transitionId = Pair(event, currentState)
        val transition = graph[transitionId] ?: return false

        check(!inTransition) { "previous transition is still in progress" }

        val len = transition.size
        for (i in 0 until len) {
            inTransition = (i < (len - 1))
            val oldState = currentState
            val newState = transition[i]
            currentState = newState
            for (listener in ArrayList(listeners)) {
                listener.onStateChanged(oldState, newState)
            }
        }

        return true
    }

    @Synchronized
    fun getCurrentState(): State {
        return currentState
    }

    /**
     * A transition defines its identity as a pair of the [`event`][event] and the starting state
     * (the first item in the [`statePath`][statePath]). `StateMachine` allows unique transitions
     * only (each transition must have a unique identity).
     *
     * @param event [`Event`][Event] - triggering event for this transition.
     * @param statePath a list of states for this transition.
     *                  First item is a starting state for the transition.
     *                  Must have at least two items, and all items must be unique.
     *
     * @throws [IllegalArgumentException] if statePath is empty or has a single item
     * @throws [IllegalArgumentException] if statePath does not consist of unique items
     *
     * @param [Event] event parameter of enum type.
     * @param [State] state parameter of enum type.
     */
    class Transition<Event : Enum<Event>, State : Enum<State>>(
        val event: Event,
        val statePath: List<State>
    ) {
        val identity: Pair<Event, State>

        init {
            require(statePath.size > 1) { "statePath must contain at least 2 items" }
            require(EnumSet.copyOf(statePath).size == statePath.size) { "statePath must consist of unique items" }
            identity = Pair(event, statePath.first())
        }
    }
}
