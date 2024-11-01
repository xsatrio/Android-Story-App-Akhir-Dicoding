package com.dicoding.storyapp.helper

import androidx.test.espresso.idling.CountingIdlingResource

object AppIdlingResource {
    private const val RESOURCE = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}

inline fun <T> wrapAppIdlingResource(function: () -> T): T {
    AppIdlingResource.increment() // Set app as busy.
    return try {
        function()
    } finally {
        AppIdlingResource.decrement() // Set app as idle.
    }
}