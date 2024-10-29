package com.dicoding.storyapp.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.AppRepository
import com.dicoding.storyapp.data.Results
import com.dicoding.storyapp.data.remote.response.ListStoryItem

class MapsViewModel(repository: AppRepository) : ViewModel() {
    val getAllStoriesWithLocation: LiveData<Results<List<ListStoryItem>>> = repository.getAllStoriesWithLoc()
}