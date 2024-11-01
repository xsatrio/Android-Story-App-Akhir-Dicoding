package com.dicoding.storyapp.ui.home

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dicoding.storyapp.R
import com.dicoding.storyapp.helper.AppIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// LogoutTesting
@RunWith(AndroidJUnit4::class)
class HomeActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(HomeActivity::class.java)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(AppIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(AppIdlingResource.countingIdlingResource)
    }

    @Test
    fun testLogout() {
        runBlocking {
            // Tekan tombol logout
            onView(withId(R.id.logout)).perform(click())

            // Verifikasi bahwa snackbar tampil
            onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(isDisplayed()))
        }
    }
}
