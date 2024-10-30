package com.dicoding.storyapp.ui.home

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.storyapp.R
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.data.local.pref.UserPref
import com.dicoding.storyapp.dataStore
import com.dicoding.storyapp.databinding.ActivityHomeBinding
import com.dicoding.storyapp.ui.adapter.LoadingStateAdapter
import com.dicoding.storyapp.ui.adapter.StoriesAdapter
import com.dicoding.storyapp.ui.addstory.AddStoryActivity
import com.dicoding.storyapp.ui.main.MainActivity
import com.dicoding.storyapp.ui.maps.MapsActivity
import com.dicoding.storyapp.widget.StoryAppWidget
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val viewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var storyAdapter: StoriesAdapter

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.map -> {
                intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.language -> {
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(intent)
                true
            }

            R.id.logout -> {
                lifecycleScope.launch {
                    val userPref = UserPref.getInstance(dataStore)
                    userPref.clearToken()

                    Snackbar.make(binding.root, R.string.logout_success, Snackbar.LENGTH_SHORT)
                        .show()
                    updateWidget()
                    delay(1000)
                    val intent = Intent(this@HomeActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storyAdapter = StoriesAdapter()

        observeViewModel()
        updateWidget()

        binding.fab.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }

        binding.rvStory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = storyAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter { storyAdapter.retry() }
            )
        }
    }

    private fun observeViewModel() {
        viewModel.getAllStories.observe(this) {
            storyAdapter.submitData(lifecycle, it)
        }

        storyAdapter.addLoadStateListener { loadState ->
            binding.progressBar.visibility = if (loadState.refresh is LoadState.Loading && storyAdapter.itemCount == 0) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (loadState.refresh is LoadState.Error) {

                val error = (loadState.refresh as LoadState.Error).error
                Snackbar.make(binding.root, error.localizedMessage ?: getString(R.string.error_loading_data), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.retry)) {
                        storyAdapter.retry()
                    }
                    .show()
            } else {
                binding.errorTextView.visibility = View.GONE
            }
        }
    }


    private fun updateWidget() {
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, StoryAppWidget::class.java)
        )
        AppWidgetManager.getInstance(application)
            .notifyAppWidgetViewDataChanged(ids, R.id.stack_view)
        Log.d("Loginctivity", "Widget IDs: ${ids.joinToString()}")
        val intent = Intent(this, StoryAppWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            Log.d("Loginctivity", "Sending broadcast for widget update")
        }
        sendBroadcast(intent)
        Log.d("Loginctivity", "Broadcast sent for widget update")
    }

    override fun onResume() {
        super.onResume()
        observeViewModel()
    }
}
