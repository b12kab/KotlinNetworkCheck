package com.example.netcheck.data

import okhttp3.ResponseBody
import retrofit2.Response

//https://stackoverflow.com/questions/67812248/elegant-way-of-handling-error-using-retrofit-kotlin-flow
sealed class NetworkResult<out T> {
    class Success<T>(response: Response<T>?): NetworkResult<T>() {
        val data = response?.body()
    }

    class Failure<T>(response: Response<T>?): NetworkResult<T>() {
        val failure:RetroErrorInfo = RetroErrorInfo(
            msg = null,
            errorBody = response?.errorBody(),
            httpStatusCode = if (response != null) response.code() else -1
        )
    }

    class Exception<T>(thro: Throwable): NetworkResult<T>() {
        val throwable: Throwable = thro
    }

    class Loading(isLoading: Boolean): NetworkResult<Nothing>() {
        val loading = isLoading
    }
}

data class RetroErrorInfo (
    val msg: String? = null,
    val errorBody: ResponseBody? = null,
    val httpStatusCode: Int = 0
)
