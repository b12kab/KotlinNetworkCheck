package com.example.netcheck

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.example.netcheck.data.NetworkResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class NetworkViewModel(
    private val repository: QuoteRepository
) : ViewModel() {
    private val TAG = this::class.java.simpleName

    var networkEnabled = false
    var initNetworkDone = false

    var _networkResults: MutableLiveData<NetworkResult<Any>> = MutableLiveData()
    val networkResults: LiveData<NetworkResult<Any>>
        get() = _networkResults

    val workerScope = CoroutineScope(Job() + Dispatchers.IO)
    val mainScope = CoroutineScope(Job() + Dispatchers.Main)
    var networkFlow: Flow<NetworkResult<Any>>? = null

    init {
        repository.scope = workerScope
    }

    fun getJob() = repository.job

    fun getDataFetchStarted() = repository.dataFetchStarted

    fun getDataFetchCompleted() = repository.dataFetchCompleted

    fun getDataFetchNetworkCompleted() = repository.networkFetchComplete

    fun initRepositoryFetchStart() {
        if (networkEnabled && !initNetworkDone) {
            Log.e(TAG, "initNetworkStart - networkEnabled and !initNetworkDone")
            repository.initializeDataSource()
            initNetworkDone = true
        }
    }

    @MainThread
    suspend fun collectData() {
        networkFlow = repository.networkResults
        try {
            networkFlow!!
                .onStart {
                    _networkResults.value = NetworkResult.Loading(true)
                }
                .onEach {
                    Log.e(
                        TAG,
                        "collectData - onEach - active: ${currentCoroutineContext().isActive}"
                    )
                    currentCoroutineContext().ensureActive()
                }
                .collect { value ->
                    Log.e(TAG, "collectData - observeNetworkData - received: {$value}")
                    _networkResults.value = value;
                }
        } catch (c: CancellationException) {
            Log.e(TAG, "collectData - observeNetworkData - CancellationException: {${c.message}}")
            withContext(NonCancellable) {
                Log.e(TAG, "collectData - observeNetworkData - CancellationException - withContext")
                workerScope.launch {
                    Log.e(TAG, "collectData - observeNetworkData - CancellationException - launch")
                    repository.cancelDataJob()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "collectData - observeNetworkData - exception: {${e.message}}")
            _networkResults.value = NetworkResult.Exception(e)
        }
    }

    @MainThread
    fun startLiveDataCollection() {
        Log.e(TAG, "startLiveDataCollection - before collectData job launch")
        mainScope.launch {
            collectData()
        }
        Log.e(TAG, "startLiveDataCollection - after collectData job launch - job started: ${getJob()?.start()}, active: ${getJob()?.isActive}, completed: ${getJob()?.isCompleted}, cancelled: ${getJob()?.isCancelled}")
    }

    @MainThread
    fun cancelJob() {
        Log.e(TAG, "cancelJob - before cancel job launch")
        workerScope.launch {
            repository.cancelDataJob()
        }
        Log.e(TAG, "cancelJob - after cancel job launch - job started: ${getJob()?.start()}, active: ${getJob()?.isActive}, completed: ${getJob()?.isCompleted}, cancelled: ${getJob()?.isCancelled}")
    }

    fun cancelFlo() {
        Log.e(TAG, "cancelFlo - initial")
        if (networkFlow == null) {
            Log.e(TAG, "cancelFlo - networkFlow is null")
            return
        }
//        if (networkFlow.cancellable().)
        Log.e(TAG, "cancelFlo - after cancel job launch - job started: ${getJob()?.start()}, active: ${getJob()?.isActive}, completed: ${getJob()?.isCompleted}, cancelled: ${getJob()?.isCancelled}")
    }

}