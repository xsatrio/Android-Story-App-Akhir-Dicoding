package com.dicoding.storyapp.widget

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.AppRepository
import com.dicoding.storyapp.data.Results
import com.dicoding.storyapp.data.remote.response.ListStoryItem
import com.dicoding.storyapp.di.Injection
import kotlinx.coroutines.runBlocking
import android.widget.AdapterView
import com.squareup.picasso.Picasso

class StackRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var storyItems = mutableListOf<ListStoryItem>()
    private lateinit var repository: AppRepository

    override fun onCreate() {
        repository = Injection.provideAppRepository(context)
        Log.d("StackRemoteViewsFactory Widget", "onCreate called")
        onDataSetChanged()
    }

    override fun onDataSetChanged() {
        Log.d("StackRemoteViewsFactory Widget", "onDataSetChanged called")
        runBlocking {
            when (val result = repository.getAllStoriesWidget()) {
                is Results.Success -> {
                    storyItems.clear()
                    storyItems.addAll(result.data)
                    Log.d("StackRemoteViewsFactory", "Data size: ${storyItems.size}")
                }
                is Results.Error -> {
                    storyItems.clear()
                    Log.d("StackRemoteViewsFactory", "Error: ${result.error}")
                }
                is Results.Loading -> { TODO()}
            }
        }
    }


    override fun onDestroy() {
        storyItems.clear()
    }

    override fun getCount(): Int = storyItems.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position == AdapterView.INVALID_POSITION || storyItems.isEmpty()) {
            return null
        }

        val story = storyItems[position]
        val views = RemoteViews(context.packageName, R.layout.widget_item)

        val imageUrl = story.photoUrl
        val bitmap = getBitmapFromUrl(imageUrl)

        if (bitmap != null) {
            Log.d("StackRemoteViewsFactory", "bitmap : $bitmap")
            views.setImageViewBitmap(R.id.imageItem, bitmap)
        } else {
            Log.d("StackRemoteViewsFactory", "Failed to load bitmap")
            views.setImageViewResource(R.id.imageItem, R.drawable.ic_error)
        }

        return views
    }


    override fun getLoadingView(): RemoteViews {
        return RemoteViews(context.packageName, R.layout.story_widget_loading)
    }

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}

private fun getBitmapFromUrl(url: String): Bitmap? {
    return try {
        Picasso.get()
            .load(url)
            .get()
    } catch (e: Exception) {
        null
    }
}