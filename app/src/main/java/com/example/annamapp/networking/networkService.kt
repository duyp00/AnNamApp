package com.example.annamapp.networking

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface NetworkService {
    /*@PUT
    suspend fun generateToken(
        @Url url: String = "https://egsbwqh7kildllpkijk6nt4soq0wlgpe.lambda-url.ap-southeast-1.on.aws/",
        @Body email: UserCredential
    ): ResponseJSON*/

    @GET
    suspend fun fetchAudio(
        @Url url: String = "https://translate.google.com/translate_tts",
        @Query("q") text: String,
        @Query("tl") language: String,
        @Query("client") client: String = "tw-ob",
        @Query("ie") encoding: String = "UTF-8"
    ): Response<ResponseBody>
}