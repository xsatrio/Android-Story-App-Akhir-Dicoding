package com.dicoding.storyapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.*
import com.dicoding.storyapp.data.local.database.StoryDatabase
import com.dicoding.storyapp.data.local.database.StoryEntity
import com.dicoding.storyapp.data.local.pref.UserPref
import com.dicoding.storyapp.data.remote.StoriesRemoteMediator
import com.dicoding.storyapp.data.remote.response.ListStoryItem
import com.dicoding.storyapp.data.remote.response.LoginResponse
import com.dicoding.storyapp.data.remote.response.RegisterResponse
import com.dicoding.storyapp.data.remote.response.Story
import com.dicoding.storyapp.data.remote.response.StoryUploadResponse
import com.dicoding.storyapp.data.remote.retrofit.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.IOException

class AppRepository(
    private val apiService: ApiService,
    private val pref: UserPref,
    private val database: StoryDatabase,
) {

    suspend fun login(email: String, password: String): Results<LoginResponse> {
        return try {
            val response = apiService.login(email, password)
            Results.Success(response)
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, LoginResponse::class.java)
            Results.Error(errorBody.message.toString())
        } catch (e: Exception) {
            Results.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun saveToken(token: String) {
        pref.saveToken(token)
    }

    suspend fun register(name: String, email: String, password: String): Results<RegisterResponse> {
        return try {
            val response = apiService.register(name, email, password)
            Results.Success(response)
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, RegisterResponse::class.java)
            Results.Error(errorBody.message.toString())
        } catch (e: Exception) {
            Results.Error(e.message ?: "An error occurred")
        }
    }

    fun getPagingStories(): LiveData<PagingData<StoryEntity>> = liveData {
        val token = pref.getToken().first()
        @OptIn(ExperimentalPagingApi::class)
        emitSource(
            Pager(
                config = PagingConfig(pageSize = 5),
                remoteMediator = token?.let { StoriesRemoteMediator(database, apiService, it) },
                pagingSourceFactory = { database.storyDao().getAllStory() }
            ).liveData
        )
    }

    fun getDetailStory(storyId: String): LiveData<Results<Story>> = liveData {
        emit(Results.Loading)
        try {
            val token = pref.getToken().first()
            Log.d("AppRepository", "Bearer token: $token")
            val response = apiService.getDetailStory(
                token = "Bearer $token",
                id = storyId
            )
            Log.d("DetailStory", response.toString())
            val story = response.story
            if (story != null) {
                emit(Results.Success(story))
            } else {
                emit(Results.Error("Story not found"))
            }
        } catch (e: Exception) {
            Log.d("DetailStory", e.toString())
            emit(Results.Error(e.message.toString()))
        }
    }

    suspend fun uploadStory(
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody?,
        lon: RequestBody?
    ): Results<StoryUploadResponse> {
        return try {
            val token = pref.getToken().first()
            Log.d("AppRepository", "Bearer token: $token")
            val response = apiService.uploadStory("Bearer $token", description, photo, lat, lon)

            if (!response.error!!) {
                Results.Success(response)
            } else {
                Results.Error(response.message ?: "Unknown error occurred")
            }
        } catch (e: IOException) {
            Results.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            Results.Error("HTTP error: ${e.message}")
        } catch (e: Exception) {
            Results.Error("An unexpected error occurred: ${e.message}")
        }
    }

    suspend fun getAllStoriesWidget(): Results<List<ListStoryItem>> {
        return try {
            val token = pref.getToken().first()
            val response = apiService.getAllStories(
                token = "Bearer $token",
                page = null,
                size = null,
                location = 0
            )
            Results.Success(response.listStory)
        } catch (e: Exception) {
            Results.Error(e.message.toString())
        }
    }

    fun getAllStoriesWithLoc(): LiveData<Results<List<ListStoryItem>>> = liveData {
        emit(Results.Loading)
        try {
            val token = pref.getToken().first()
            val response = apiService.getStoriesWithLocation(
                token = "Bearer $token",
                location = 1
            )
            emit(Results.Success(response.listStory))
        } catch (e: Exception) {
            emit(Results.Error(e.message.toString()))
        }
    }

    companion object {
        @Volatile
        private var instance: AppRepository? = null
        fun getInstance(
            apiService: ApiService,
            pref: UserPref,
            database: StoryDatabase,
        ): AppRepository =
            instance ?: synchronized(this) {
                instance ?: AppRepository(apiService, pref, database)
            }.also { instance = it }
    }
}
