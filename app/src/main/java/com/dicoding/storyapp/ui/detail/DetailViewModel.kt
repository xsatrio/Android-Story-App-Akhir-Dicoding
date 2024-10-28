package com.dicoding.storyapp.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.AppRepository
import com.dicoding.storyapp.data.Results
import com.dicoding.storyapp.data.remote.response.DetailResponse
import com.dicoding.storyapp.data.remote.response.Story

class DetailViewModel(private val repository: AppRepository) : ViewModel() {

    private val _story = MutableLiveData<DetailResponse>()
    val story: LiveData<DetailResponse> = _story

    fun getDetailStory(storyId: String): LiveData<Results<Story>> {
        return repository.getDetailStory(storyId)
    }
}
