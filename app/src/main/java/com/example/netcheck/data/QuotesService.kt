package com.example.netcheck.data

import com.example.netcheck.data.model.QuoteList
import retrofit2.Response
import retrofit2.http.GET

interface QuotesService {
    @GET("/quotes")
    suspend fun getQuotes() : Response<QuoteList>
}