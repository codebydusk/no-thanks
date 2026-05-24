package com.github.codebydusk.nothanks.data

import retrofit2.http.GET

data class ExcuseResponse(
    val reason: String
)

interface ExcuseApi {
    @GET("no")
    suspend fun getExcuse(): ExcuseResponse
}
