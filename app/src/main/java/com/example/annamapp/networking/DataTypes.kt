package com.example.annamapp.networking

import kotlinx.serialization.Serializable

//@Serializable
//data class UserEmail(val email: String)

//@Serializable
//data class UserToken(val token: String)

//@Serializable
//data class AudioRequestJSON(val word: String, val email: String, val token: String)

@Serializable
data class ResponseJSON(val code: Int, val message: String)