package com.dicoding.storyapp.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.AppRepository
import com.dicoding.storyapp.data.Results
import com.dicoding.storyapp.data.remote.response.RegisterResponse
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: AppRepository) : ViewModel() {

    fun register(name: String, email: String, password: String, onSuccess: (RegisterResponse) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val results = repository.register(name, email, password)) {
                is Results.Loading -> TODO()
                is Results.Success -> {
                    val registerResult = results.data
                    onSuccess(registerResult)
                }
                is Results.Error -> {
                    onError(results.error)
                }
            }
        }
    }

}
