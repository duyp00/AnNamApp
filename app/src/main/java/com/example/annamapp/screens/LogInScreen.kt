package com.example.annamapp.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.annamapp.ui.NetworkService
import com.example.annamapp.ui.UserCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun LogInScreen(
    onMessageChange: (String) -> Unit = {},
    networkService: NetworkService
) {
    LaunchedEffect(Unit) {
        onMessageChange("get tokens to log in")
    }

    val scope = rememberCoroutineScope()
    var token by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }

    Column {
        TextField(
            value = token,
            onValueChange = {/* only supposed to show data from network response */},
            modifier = Modifier.semantics { contentDescription = "tokenfield" },
            label = { Text("token") },
            readOnly = true
        )
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.semantics { contentDescription = "emailfield" },
            label = { Text("email") }
        )

        Button(onClick = {
            scope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val result = networkService.generateToken(
                            email = UserCredential(email)
                        )
                        token = result.token
                        onMessageChange("The token has been received successfully.")
                        Log.d("FLASHCARD", result.toString())

                    } catch (e: Exception) {
                        onMessageChange("There was an error in the token request.")
                        Log.d("FLASHCARD", "Unexpected exception: $e")
                    }
                }
            }
        },
            modifier = Modifier.semantics { contentDescription = "Enter" }
        )
        { Text("Enter") }
    }
}