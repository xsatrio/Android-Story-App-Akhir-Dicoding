package com.dicoding.storyapp.ui.login

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.dicoding.storyapp.R
import com.dicoding.storyapp.helper.AppIdlingResource
import com.dicoding.storyapp.ui.home.HomeActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Login Testing
@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
        IdlingRegistry.getInstance().register(AppIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        Intents.release()
        IdlingRegistry.getInstance().unregister(AppIdlingResource.countingIdlingResource)
    }

    @Test
    fun loginFailure() {
        onView(withId(R.id.emailEditTextLogin)).perform(typeText("wrong@example.com"), closeSoftKeyboard())
        onView(withId(R.id.passwordEditTextLogin)).perform(typeText("wrongpassword"), closeSoftKeyboard())

        onView(withId(R.id.loginButton)).perform(click())

        // Verifikasi pesan kesalahan ditampilkan
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(isDisplayed()))
    }


    @Test
    fun loginSuccess() {
        // Masukkan email dan password yang benar
        onView(withId(R.id.emailEditTextLogin)).perform(click(), typeText("satrio0000@p.com"), closeSoftKeyboard())
        onView(withId(R.id.passwordEditTextLogin)).perform(typeText("pppppppp"), closeSoftKeyboard())

        // Tekan tombol login
        onView(withId(R.id.loginButton)).perform(click())

        // Verifikasi pindah ke HomeActivity
        intended(hasComponent(HomeActivity::class.java.name))
    }
}
