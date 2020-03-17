package vit.khudenko.android.fsm.test_utils

import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.mockito.ArgumentMatcher

fun <T : Throwable> assertThrows(
    expectedThrowable: Class<T>,
    expectedThrowableMessage: String?,
    action: () -> Unit
): T {
    val expectedThrowableMessageMatcher = if (expectedThrowableMessage != null) {
        equals(expectedThrowableMessage)
    } else {
        isNull()
    }
    return assertThrows(expectedThrowable, expectedThrowableMessageMatcher, action)
}

fun <T : Throwable> assertThrows(
    expectedThrowable: Class<T>,
    expectedThrowableMessageMatcher: ArgumentMatcher<String>,
    action: () -> Unit
): T {
    val actualThrowable = assertThrows(null, expectedThrowable) { action.invoke() }
    assertTrue("unexpected throwable message", expectedThrowableMessageMatcher.matches(actualThrowable.message))
    return actualThrowable
}
