package com.example.netcheck

import android.util.Log
import androidx.annotation.WorkerThread
import com.example.netcheck.data.NetworkResult
import com.example.netcheck.data.QuotesService
import com.example.netcheck.data.RetrofitHelper
import com.example.netcheck.data.model.QuoteList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okio.IOException
import retrofit2.Response
import retrofit2.Retrofit
import java.net.UnknownHostException

class QuoteRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val TAG = this::class.java.simpleName
    private lateinit var results: Response<QuoteList>

    var dataFetchStarted = false
    var dataFetchCompleted = false
    var networkFetchComplete = false

    var networkResults: Flow<NetworkResult<Any>> = emptyFlow()

    var scope: CoroutineScope? = null;
    var job: Job? = null;

    val jobStatus: () -> Boolean
        get() = {
            when (job?.isActive) {
                null -> {
                    Log.e(TAG, "jobStatus - job isActive is null")
                    false
                }
                true -> {
                    Log.e(TAG, "jobStatus - job isActive is true")
                    true
                }
                else -> {
                    Log.e(TAG, "jobStatus - job isActive is false")
                    false
                }
            }
        }

    @WorkerThread
    fun initializeDataSource() {
        Log.e(TAG, "initialize - before job launch")
//        job = scope?.launch {
        scope?.launch {
            networkResults = getData()
        }
        Log.e(TAG, "initialize - after job: $job - job started: ${job?.start()}, active: ${job?.isActive}, completed: ${job?.isCompleted}, cancelled: ${job?.isCancelled}")
    }

    @WorkerThread
    fun getData(): Flow<NetworkResult<Any>> =  flow {
        try {
            // Before this point, the job is null or set to something which when the flow is started is incorrect.
            // This will set it to the correct value, so that the variable can be access the appropriate job value
            // and possibly used to cancel the remainder of the network calls - if used.
            job = currentCoroutineContext().job

            // Setup the flags
            Log.e(TAG,"getData - before getting service instance")
            Log.e(TAG,"getData - job: $job - job started: ${job?.start()}, active: ${job?.isActive}, completed: ${job?.isCompleted}, cancelled: ${job?.isCancelled}")
            dataFetchStarted = true
            dataFetchCompleted = false

            val quotesService = RetrofitHelper.getInstance().create(QuotesService::class.java)

            Log.e(TAG,"getData - after getting service instance")
            Log.e(TAG,"getData - job: $job - job started: ${job?.start()}, active: ${job?.isActive}, completed: ${job?.isCompleted}, cancelled: ${job?.isCancelled}")
            Log.e(TAG,"getData - scope: $scope - active: ${scope?.isActive}")

            for (netTry in 1..3) {
                try {
                    Log.e(TAG,"getData - network try ${netTry} - before coroutine active check")
                    Log.e(TAG,"getData - jobStatus: ${jobStatus()}")
                    Log.e(TAG,"getData - job: $job - job started: ${job?.start()}, active: ${job?.isActive}, completed: ${job?.isCompleted}, cancelled: ${job?.isCancelled}")
                    Log.e(TAG,"getData - scope: $scope - active: ${scope?.isActive}")
                    Log.e(TAG,"getData - active: ${currentCoroutineContext().isActive}")
                    Log.e(TAG,"getData - job [CURRENT Context]: ${currentCoroutineContext().job} - job started: ${currentCoroutineContext().job?.start()}, active: ${currentCoroutineContext().job?.isActive}, completed: ${currentCoroutineContext().job?.isCompleted}, cancelled: ${currentCoroutineContext().job?.isCancelled}")

                    yield()
                    for (i in 5 downTo 1) {
                        networkFetchComplete = false

                        Log.e(TAG, "getData - network try ${netTry} - before getQuotes, i = $i")
                        results = quotesService.getQuotes(i)
                        Log.e(TAG,"getData - network try ${netTry} - after getQuotes - before sleep")

                        if (results != null && results.isSuccessful) {
//                            Log.e(TAG,"getData - network try ${netTry} - result successful. i = $i; Result data: ${results.body().toString()}")
                            Log.e(TAG,"getData - network try ${netTry} - result successful. i = $i")
                            emit(NetworkResult.Success(results))
                        } else {
                            Log.e(TAG, "getData - network try ${netTry} - result NOT successful. i = $i")
                            emit(NetworkResult.Failure(results))
                        }

                        Log.e(TAG,"getData - network try ${netTry} - after after emit - before delay")
                        delay(5000);
                        Log.e(TAG, "getData - network try ${netTry} - after delay")
                        yield()
                        networkFetchComplete = true
                    }
                    Log.e(TAG, "getData - network try ${netTry} - network - after network section")
                } catch (uhe: UnknownHostException) {
                    Log.e(TAG, "getData - network try ${netTry} - network - UnknownHostException: $uhe")
                    emit(NetworkResult.Exception(uhe))
                    // cancel the flow
                    currentCoroutineContext().cancel()
                } catch (io: IOException) {
                    Log.e(TAG, "getData - network try ${netTry} - network - IOException: $io")
                    emit(NetworkResult.Exception(io))
                    // cancel the flow
                    currentCoroutineContext().cancel()
                } catch (e: Exception) {
                    Log.e(TAG, "getData - network try ${netTry} - network - exception: $e")
                    throw e
                }

                if (networkFetchComplete) {
                    Log.e(TAG, "getData - after inner try, networkFetchComplete = true")
                    break
                }

                Log.e(TAG, "getData - after networkFetchComplete check")
            }

            dataFetchStarted = false
            dataFetchCompleted = true

            Log.e(TAG,"getData - after loop / after sleep")
            Log.e(TAG,"getData - job: $job - job started: ${job?.start()}, active: ${job?.isActive}, completed: ${job?.isCompleted}, cancelled: ${job?.isCancelled}")
            Log.e(TAG,"getData - scope: $scope - active: ${scope?.isActive}")
            Log.e(TAG,"getData - active: ${currentCoroutineContext().isActive}")
            Log.e(TAG,"getData - job [CURRENT Context]: ${currentCoroutineContext().job} - job started: ${currentCoroutineContext().job?.start()}, active: ${currentCoroutineContext().job?.isActive}, completed: ${currentCoroutineContext().job?.isCompleted}, cancelled: ${currentCoroutineContext().job?.isCancelled}")

            yield()
            Log.e(TAG, "getData - after yield, before networkFetchComplete check")
            if (!networkFetchComplete) {
                Log.e(TAG, "getData - NOT - networkFetchComplete failed - network fetch failed for some reason")
                emit(NetworkResult.Failure(null))
            }
        } catch (c: CancellationException) {
            // do nothing
        } catch (e: Exception) {
            Log.e(TAG, "getData - exception: $e")
            emit(NetworkResult.Exception(e))
        }
    }.cancellable().flowOn(ioDispatcher)

    @WorkerThread
    fun cancelDataJob() {
        Log.e(TAG, "cancelDataSource - initial")
        if (job != null && job?.isActive == true) {
            job?.cancel()
        }
        Log.e(TAG, "cancelDataSource - after cancel - active: ${job?.isActive}, completed: ${job?.isCompleted}, cancelled: ${job?.isCancelled}")
    }

}
