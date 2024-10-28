package com.dicoding.storyapp.ui.main

import android.animation.ObjectAnimator
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.pref.UserPref
import com.dicoding.storyapp.databinding.ActivityMainBinding
import com.dicoding.storyapp.ui.home.HomeActivity
import com.dicoding.storyapp.ui.login.LoginActivity
import com.dicoding.storyapp.ui.register.RegisterActivity
import com.dicoding.storyapp.widget.StoryAppWidget
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userPref: UserPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLoginStatus()
        updateWidget()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playAnimation()

        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.signupButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun checkLoginStatus() {
        userPref = UserPref.getInstance(dataStore)
        lifecycleScope.launch {
            userPref.getToken().collect { token ->
                if (!token.isNullOrEmpty()) {
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun updateWidget() {
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, StoryAppWidget::class.java)
        )
        AppWidgetManager.getInstance(application).notifyAppWidgetViewDataChanged(ids, R.id.stack_view)
        Log.d("HomeActivity", "Widget IDs: ${ids.joinToString()}")
        val intent = Intent(this, StoryAppWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            Log.d("HomeActivity", "Sending broadcast for widget update")
        }
        sendBroadcast(intent)
        Log.d("HomeActivity", "Broadcast sent for widget update")
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
    }
}
