package com.example.annamapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.example.annamapp.EMAIL
import com.example.annamapp.TOKEN
import com.example.annamapp.dataStore
import com.example.annamapp.networking.ResponseJSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LogInScreen(
    onMessageChange: (String) -> Unit = {},
    //networkService: NetworkService,
    onNavigateToPrevious: () -> Unit
) {
    LaunchedEffect(Unit) {
        onMessageChange("get tokens to log in")
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext
    //var token by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var token by rememberSaveable { mutableStateOf("") }

    Column {
        /*
        TextField(
            value = token,
            onValueChange = {/* only supposed to show data from network response */},
            modifier = Modifier.semantics { contentDescription = "tokenfield" },
            label = { Text("token") },
            readOnly = true
        )*/
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.semantics { contentDescription = "emailfield" },
            label = { Text("email") }
        )
        TextField(
            value = token,
            onValueChange = { token = it },
            modifier = Modifier.semantics { contentDescription = "tokenfield" },
            label = { Text("token") }
        )
        Row {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val resMessage = withContext(Dispatchers.IO) {
                                val result = ResponseJSON(code = 200, message = "this is a demo. enter any token")
                                //networkService.generateToken(email = UserCredential(email))
                                listOf(result.code.toString(), result.message)//.joinToString(", ")
                            }
                            onMessageChange("Response code = ${resMessage[0]}, message = ${resMessage[1]}")
                        } catch (e: Exception) {
                            onMessageChange("Error in the token request: $e")
                            //return@launch //not necessary because it's the last statement
                        }
                    }
                },
                modifier = Modifier.semantics { contentDescription = "GetToken" }
            ) { Text("Get token") }
            Spacer(modifier = Modifier.width(5.dp))
            Button(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            appContext.dataStore.edit { preferences ->
                                preferences[EMAIL] = email
                                preferences[TOKEN] = token
                            }
                        }
                        //onMessageChange("Logged in")
                        onNavigateToPrevious()
                    }
                },
                modifier = Modifier.semantics { contentDescription = "LogIn" }
            ) { Text("Log in") }
        }
    }
}