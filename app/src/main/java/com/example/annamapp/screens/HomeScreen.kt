package com.example.annamapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.annamapp.EMAIL
import com.example.annamapp.TOKEN
import com.example.annamapp.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToStudy: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToLogIn: () -> Unit,
    onMessageChange: (String) -> Unit = {}
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext
    LaunchedEffect(Unit) {
        //onMessageChange("this is home screen")
        val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
        val preferences = preferencesFlow.first()
        onMessageChange(preferences[EMAIL] ?: "")
    }
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Menu",
            style = MaterialTheme.typography.headlineMedium, // M3 typography scale
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = onNavigateToStudy,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription = "navigateToStudyCards"
                }
        ) {
            Text("Study Cards")
        }

        Button(
            onClick = onNavigateToAdd,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription = "navigateToAddCard"
                }
        ) {
            Text("Add a Card")
        }

        Button(
            onClick = onNavigateToSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription = "navigateToSearchCards"
                }
        ) {
            Text("Search Cards")
        }

        Button(
            onClick = onNavigateToLogIn,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription = "navigateToLogIn"
                }
        ) {
            Text("Log in")
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "ExecuteLogout" }, onClick = {

                scope.launch {
                    appContext.dataStore.edit { preferences ->
                        preferences.remove(EMAIL)
                        preferences.remove(TOKEN)
                        onMessageChange(preferences[EMAIL] ?: "")
                    }
                }

            }) {
            Text(
                "Log out",
                modifier = Modifier.semantics { contentDescription = "Logout" }
            )
        }
    }
}