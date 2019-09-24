package vit.khudenko.android.fsm

import java.util.Collections
import java.util.EnumSet

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
 *            Session.Event.LOGIN,
 *            listOf(Session.State.INACTIVE, Session.State.ACTIVE)
 *        )
 *        .addTransition(
 *            Session.Event.LOGOUT,
 *            listOf(Session.State.ACTIVE, Session.State.INACTIVE)
 *        )
 *        .addTransition(
 *            Session.Event.LOGOUT_AND_FORGET,
 *            listOf(Session.State.ACTIVE, Session.State.FORGOTTEN)
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
         * Each transition defines its identity as a combination of the [`event`][event] and the starting state
         * (the first item in the [`statePath`][statePath]). `StateMachine` allows unique transitions only.
         *
         * @param event [`Event`][Event].
         * @param statePath a list of states.
         *
         * @return [`StateMachine.Builder`][StateMachine.Builder]
         *
         * @throws [StateMachineBuilderValidationException] if statePath is empty or has a single state
         * @throws [StateMachineBuilderValidationException] if statePath does not consist of unique states
         * @throws [StateMachineBuilderValidationException] if a duplicate transition identified (by a combination
         *                                                  of event and starting state)
         */
        fun addTransition(event: Event, statePath: List<State>): Builder<Event, State> {
            val statePathCopy = statePath.toMutableList()

            if (statePathCopy.size < 2) {
                throw StateMachineBuilderValidationException("statePath must have at least 2 states")
            }

            if (EnumSet.copyOf(statePathCopy).size != statePathCopy.size) {
                throw StateMachineBuilderValidationException("statePath must consist of unique states")
            }

            val startState = statePathCopy.removeAt(0)
            val transitionId = Pair(event, startState)

            if (graph.containsKey(transitionId)) {
                val cause = "statePath with the same start state $startState is already defined for the event $event"
                throw StateMachineBuilderValidationException(cause)
            }

            graph[transitionId] = Collections.unmodifiableList(statePathCopy)

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
         * @throws [StateMachineBuilderValidationException] if initial state has not been set
         * @throws [StateMachineBuilderValidationException] if no transitions have been added
         */
        fun build(): StateMachine<Event, State> {
            if (this::initialState.isInitialized.not()) {
                throw StateMachineBuilderValidationException("initialState must be set")
            }
            if (graph.isEmpty()) {
                throw StateMachineBuilderValidationException("at least one transition must be added")
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

        check(!inTransition) { "can not start a new transition - there is an on-going unfinished transition" }

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
}
