package com.example.netcheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class NetworkViewModelFactory(
    private val repository: QuoteRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(NetworkViewModel::class.java)){
            return NetworkViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown View Model class")
    }
}