`StateMachine` is a general purpose finite-state machine written in Kotlin.

### Integration

At the project level `build.gradle`, add a maven repo pointing to `https://dl.bintray.com/vit-khudenko/libs`, e.g.:

```groovy
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://dl.bintray.com/vit-khudenko/libs' } // this is it
    }
}
```

At a module level `build.gradle`, add the following dependency:

```groovy
implementation 'vit.khudenko.android:fsm:0.1.0'
```

##### Java projects

If your app is written in Java, then, at the module level `build.gradle`, also add `Kotlin Standard 
Library` dependency.
Note, there are different types of the library, each corresponding to the Java APIs used by your app.
As of 2019-09-25, it would be one of the following (assuming the current Kotlin version is `1.3.50`):

```groovy
implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.3.50'      // for JDK6 APIs
implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.50' // for JDK7 APIs
implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.50' // for JDK8 APIs
```

### Usage

A sample configuration, assuming your app has `Session` class, that defines __specific 
to your app__ events and states (`Session.Event` and `Session.State` enums):

```kotlin
val sessionStateMachine = StateMachine.Builder<Session.Event, Session.State>()
    .setInitialState(Session.State.ACTIVE)
    .addTransition(
        Session.Event.LOGIN,
        listOf(Session.State.INACTIVE, Session.State.ACTIVE)
    )
    .addTransition(
        Session.Event.LOGOUT,
        listOf(Session.State.ACTIVE, Session.State.INACTIVE)
    )
    .addTransition(
        Session.Event.LOGOUT_AND_FORGET,
        listOf(Session.State.ACTIVE, Session.State.FORGOTTEN)
    )
    .build()
```

Meaning of such sample configuration is that there are 3 possible session states 
(`ACTIVE`, `INACTIVE` and `FORGOTTEN`) and 3 possible events (`LOGIN`, `LOGOUT` and `LOGOUT_AND_FORGET`).
And using these events and states there are possible 3 state machine transitions:

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
```

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
