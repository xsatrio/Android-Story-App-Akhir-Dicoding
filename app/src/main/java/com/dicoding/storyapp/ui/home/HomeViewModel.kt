package com.dicoding.storyapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.AppRepository
import com.dicoding.storyapp.data.Results
import com.dicoding.storyapp.data.remote.response.ListStoryItem

class HomeViewModel(repository: AppRepository) : ViewModel() {
    val getAllStories: LiveData<Results<List<ListStoryItem>>> = repository.getAllStories()
}
