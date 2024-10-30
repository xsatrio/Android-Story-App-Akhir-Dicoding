package com.dicoding.storyapp.di

import android.content.Context
import com.dicoding.storyapp.data.AppRepository
import com.dicoding.storyapp.data.local.database.StoryDatabase
import com.dicoding.storyapp.data.local.pref.UserPref
import com.dicoding.storyapp.data.remote.retrofit.ApiConfig
import com.dicoding.storyapp.dataStore

object Injection {
    fun provideAppRepository(context: Context): AppRepository {
        val apiService = ApiConfig.getApiService()
        val pref = UserPref.getInstance(context.dataStore)
        val database = StoryDatabase.getDatabase(context)
        return AppRepository.getInstance(apiService, pref, database)
    }
}