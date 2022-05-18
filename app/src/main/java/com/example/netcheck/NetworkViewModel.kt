package com.example.netcheck

import android.util.Log
import androidx.lifecycle.*
import com.example.netcheck.data.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NetworkViewModel(
    private val repository: QuoteRepository
) : ViewModel() {
    private val TAG = this::class.java.simpleName

    var networkEnabled = false
    var initNetworkDone = false

    var _networkResults: MutableLiveData<NetworkResult<Any>> = MutableLiveData()
    val networkResults: LiveData<NetworkResult<Any>>
        get() = _networkResults

    val scope = CoroutineScope(Job() + Dispatchers.IO)

    init {
        repository.scope = scope
    }

    fun initNetworkStart() { //= scope.launch {
        if (networkEnabled && !initNetworkDone) {
            repository.initializeQuotesDataSource()
            initNetworkDone = true
        }
    }

    suspend fun collectData(): LiveData<NetworkResult<Any>> {
        val flo = repository.networkResults
        try {
            flo.collect { value ->
                Log.d(TAG, "observeNetworkData - received: {$value}")
            }
        } catch (e: Exception) {
            Log.d(TAG, "observeNetworkData - exception: {${e.message}}")

        }

        return liveData {  }
             //.asLiveData()
//            .onStart { _networkResults.value = NetworkResult.Loading(true) }
//            .catch { e -> _networkResults.value = NetworkResult.Exception(e) }
//            .collect { item -> _networkResults.value = item }
    }

    fun getJob() = repository.job

    fun startLiveDataCollection() {
        Log.i(TAG, "startCollection - before job launch")
        scope?.launch {
            collectData()
        }
        Log.i(TAG, "startCollection - after collectData")
    }
}