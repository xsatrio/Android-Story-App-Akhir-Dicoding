package com.dicoding.storyapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.storyapp.data.AppRepository
import com.dicoding.storyapp.data.local.database.StoryEntity

class HomeViewModel(repository: AppRepository) : ViewModel() {
    val getAllStories: LiveData<PagingData<StoryEntity>> =
        repository.getPagingStories().cachedIn(viewModelScope)
}
