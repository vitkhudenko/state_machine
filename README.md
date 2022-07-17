`StateMachine` is a general purpose finite-state machine written in Kotlin.

### Integration

At the project level `build.gradle`, add a maven repo pointing to `https://jitpack.io`, e.g.:

```groovy
allprojects {
    repositories {
        google()
        maven { url "https://jitpack.io" } // this is it
        jcenter()
    }
}
```

At a module level `build.gradle`, add the following dependency:

```groovy
implementation 'com.github.vitkhudenko:state_machine:0.3.9'
```

### Usage

A sample configuration, assuming your app has `Session` class, that defines __specific 
to your app__ events and states (`Session.Event` and `Session.State` enums):

```kotlin
val sessionStateMachine = StateMachine.Builder<Session.Event, Session.State>()
    .setInitialState(Session.State.ACTIVE)
    .addTransition(
        StateMachine.Transition(
            event = Session.Event.LOGIN,
            statePath = listOf(Session.State.INACTIVE, Session.State.ACTIVE)
        )
    )
    .addTransition(
        StateMachine.Transition(
            event = Session.Event.LOGOUT,
            statePath = listOf(Session.State.ACTIVE, Session.State.INACTIVE)
        )
    )
    .addTransition(
        StateMachine.Transition(
            event = Session.Event.LOGOUT_AND_FORGET,
            statePath = listOf(Session.State.ACTIVE, Session.State.FORGOTTEN)
        )
    )
    .build()
```

Meaning of the above sample configuration is that:
* There are 3 possible session states (`ACTIVE`, `INACTIVE` and `FORGOTTEN`).
* There are 3 possible events (`LOGIN`, `LOGOUT` and `LOGOUT_AND_FORGET`).
* StateMachine's initial state is `ACTIVE`.
* There are 3 possible state machine transitions:

|Event              |State path              |
|-------------------|------------------------|
|`LOGIN`            |`INACTIVE` -> `ACTIVE`  |
|`LOGOUT`           |`ACTIVE` -> `INACTIVE`  |
|`LOGOUT_AND_FORGET`|`ACTIVE` -> `FORGOTTEN` |

To move the sample `sessionStateMachine` to a new state your app should call `consumeEvent()` method:

```kotlin
// moves state machine from ACTIVE to INACTIVE state
sessionStateMachine.consumeEvent(Session.Event.LOGOUT)
```

State changes are propagated via `StateMachine.Listener`.

```kotlin
sessionStateMachine.addListener(object : StateMachine.Listener<Session.State> {
    override fun onStateChanged(oldState: Session.State, newState: Session.State) {
        // do something
    }
})

// or in a more concise way
sessionStateMachine.addListener { oldState, newState -> /* do something */ }
```

It is also possible to define transitions in a more concise way:

```kotlin
val sessionStateMachine = StateMachine.Builder<Session.Event, Session.State>()
    // ..
    .addTransitions(
        Session.Event.LOGIN to listOf(Session.State.INACTIVE, Session.State.ACTIVE),
        Session.Event.LOGOUT to listOf(Session.State.ACTIVE, Session.State.INACTIVE),
        Session.Event.LOGOUT_AND_FORGET to listOf(Session.State.ACTIVE, Session.State.FORGOTTEN)
    )
```

### Test coverage

[Test Coverage report][test_coverage]

### Threading

The `StateMachine` implementation is thread-safe. Public API methods are declared as `synchronized`.

The `StateMachine` is a synchronous tool, meaning it neither creates threads, nor uses thread pools or handlers.

### License

> MIT License
>
> Copyright (c) 2019 Vitaliy Khudenko
>
> Permission is hereby granted, free of charge, to any person obtaining a copy
> of this software and associated documentation files (the "Software"), to deal
> in the Software without restriction, including without limitation the rights
> to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
> copies of the Software, and to permit persons to whom the Software is
> furnished to do so, subject to the following conditions:
>
> The above copyright notice and this permission notice shall be included in all
> copies or substantial portions of the Software.
>
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
> LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
> OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
> SOFTWARE.

[test_coverage]: https://rawcdn.githack.com/vitkhudenko/state_machine/347ee35c4fd673e98e64376f5f1d3082f016f4cd/state_machine_lib/reports/kover/index.html