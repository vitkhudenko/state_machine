package vit.khudenko.android.fsm.test_utils

import org.mockito.ArgumentMatcher

fun matches(regex: String) = ArgumentMatcher<String> { argument -> argument!!.matches(regex.toRegex()) }

fun <T> equals(item: T) = ArgumentMatcher<T> { argument -> argument == item }

fun <T> isNull() = ArgumentMatcher<T> { argument -> argument == null }
