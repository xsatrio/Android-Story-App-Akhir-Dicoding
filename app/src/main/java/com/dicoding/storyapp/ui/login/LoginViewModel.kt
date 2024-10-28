package com.dicoding.storyapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.AppRepository
import com.dicoding.storyapp.data.remote.response.LoginResult
import com.dicoding.storyapp.data.Results
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AppRepository) : ViewModel() {

    fun login(email: String, password: String, onSuccess: (LoginResult) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val results = repository.login(email, password)) {
                is Results.Loading -> TODO()
                is Results.Success -> {
                    val loginResult = results.data.loginResult
                    if (loginResult != null) {
                        repository.saveToken(loginResult.token ?: "")
                        onSuccess(loginResult)
                    } else {
                        onError("Login failed: no results")
                    }
                }
                is Results.Error -> {
                    onError(results.error)
                }
            }
        }
    }
}
