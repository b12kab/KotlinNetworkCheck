package com.example.netcheck

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.example.netcheck.data.NetworkResult
import com.example.netcheck.data.QuotesService
import com.example.netcheck.data.RetrofitHelper
import com.example.netcheck.data.model.QuoteList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.Response

class QuoteRepository() {
    private val TAG = this::class.java.simpleName
    private lateinit var results: Response<QuoteList>

//    lateinit var quoteServiceResults: LiveData<Result<Any?>>
    lateinit var quoteServiceResults: Flow<Result<Any?>>

    //    private lateinit var quoteServiceResults: LiveData<Result<QuoteList?>>
//    val quoteResult: LiveData<Result<QuoteList?>>
//        get() = quoteServiceResults
//    var networkResults: LiveData<NetworkResult<Any>> = MutableLiveData()
    var networkResults: Flow<NetworkResult<Any>> = emptyFlow()

    var scope: CoroutineScope? = null;
    var job: Job? = null;

    @WorkerThread
//    suspend fun initializeQuotesDataSource() {
    fun initializeQuotesDataSource() {
        Log.i(TAG, "initialize - before job launch")
        job = scope?.launch {
            networkResults = getData()
        }
        Log.i(TAG, "initialize - after job: $job - job started: ${job?.start()}, active: ${job?.isActive}")
    }

    @WorkerThread
    fun getData(): Flow<NetworkResult<Any>> //{
//        var a = 2
//        return flow {
        =  flow {
            var b = 3
            try {
                Log.d(TAG, "getData - before sendLoading true")
//                sendLoading(true)
                emit(NetworkResult.Loading(true))
                Log.d(TAG, "getData - after sendLoading true - before getting service instance")
                val quotesService = RetrofitHelper.getInstance().create(QuotesService::class.java)
                Log.d(TAG, "getData - after getting service instance - before getQuotes")
                val result = quotesService.getQuotes()
                Log.d(TAG, "getData - after getQuotes - before sendLoading false")
    //            sendLoading(false)
    //            Log.d(TAG, "getData - after sendLoading false")
                if (result != null && result.isSuccessful) {
                    Log.d(TAG, "getData - result successful")
                    // Checking the results
                    Log.d(TAG, result.body().toString())
                    results = result;
    //                sendSuccess()
                    emit(NetworkResult.Success(results))
                } else {
                    Log.d(TAG, "getData - result NOT successful")
    //                sendBadResult()
                    emit(NetworkResult.Failure(result))
                }
            } catch (e: Exception) {
                Log.i(TAG, "getData - exception: $e")
//                sendException(e)
                emit(NetworkResult.Exception(e))
            }
        }.cancellable().flowOn(Dispatchers.IO)
//    }

//    suspend fun sendLoading(loadStatus: Boolean) {
//        Log.d(TAG, "sendLoading - before withContext")
////        withContext(Dispatchers.Main) {
////            Log.d(TAG, "sendLoading - before liveData emit")
////            networkResults = liveData {
////                Log.d(TAG, "sendLoading - before emit")
////                emit(NetworkResult.Loading(loadStatus))
////                Log.d(TAG, "sendLoading - after emit")
////            }
////            Log.d(TAG, "sendLoading - after liveData emit")
////        }
//        Log.d(TAG, "sendLoading - after withContext")
//    }

//    fun sendException(e: Exception) {
////        quoteServiceResults = liveData {
////            emit(
////                Result.failure<Exception>(e)
////            )
////        }
////        networkResults = liveData {
////            emit(NetworkResult.Exception(e))
////        }
//        networkResults = flow { emit(NetworkResult.Exception(e)) }
//    }

//    fun sendBadResult() {
////        quoteServiceResults = liveData {
////            emit(Result.failure<ResponseBody?>(results?.errorBody()))
////        }
////        networkResults = liveData {
////            emit(NetworkResult.Failure(results))
////        }
//        networkResults = flow { emit(NetworkResult.Failure(results)) }
//    }

//    fun sendSuccess() {
////        quoteServiceResults = liveData {
////            emit(Result.success(results?.body()))
////        }
////        networkResults = liveData {
////            emit(NetworkResult.Success(results))
////        }
//        networkResults = flow { emit(NetworkResult.Success(results)) }
//    }
}
