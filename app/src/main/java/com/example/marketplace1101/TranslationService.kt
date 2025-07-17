package com.example.marketplace1101

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TranslationService {
    @GET("get")
    fun translate(
        @Query("q") q: String,
        @Query("langpair") langPair: String = "en|pt"
    ): Call<MyMemoryResponse>
}

data class MyMemoryResponse(
    val responseData: ResponseData
)

data class ResponseData(
    val translatedText: String
)
