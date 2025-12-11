package com.example.annamapp.ui

import kotlinx.serialization.Serializable


@Serializable
data class UserCredential (val email: String)

@Serializable
data class UserToken (val token: String)