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
import com.example.annamapp.EMAIL
import com.example.annamapp.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Composable
fun HomeScreen(
    onNavigateToStudy: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToLogIn: () -> Unit,
    onMessageChange: (String) -> Unit = {}
) {
    //val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext
    LaunchedEffect(Unit) {
        //Then, use the DataStore.data property to expose the appropriate stored value using a Flow.

        //In coroutines, a flow is a type that can emit multiple values sequentially,
        //as opposed to suspend functions that return only a single value.
        //For example, you can use a flow to receive live updates from a database.

        //Flows are built on top of coroutines and can provide multiple values.
        //A flow is conceptually a stream of data that can be computed asynchronously.
        //The emitted values must be of the same type. For example, a Flow<Int>
        //is a flow that emits integer values.

        //In Kotlin with Jetpack DataStore, the Flow<Preferences> returned by dataStore.data
        // emits every time any single preference within the DataStore file changes.
        //The flow emits the entire Preferences object, containing all current key-value pairs, with each change.

        //In Kotlin Flow, the first() terminal operator is used to collect only the initial value emitted
        //by a flow and then automatically cancel the flow's execution.
        //This is particularly useful in Jetpack Compose and other Android development scenarios
        //where you only need a single, immediate result from a potentially long-running data stream.
        val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
        val preferences = preferencesFlow.first()
        onMessageChange(preferences[EMAIL] ?: "")
    }
    Column(
        modifier = Modifier.padding(24.dp).fillMaxSize(),
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
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                .semantics {
                    contentDescription = "navigateToStudyCards"
                }
        ) {
            Text("Study Cards")
        }

        Button(
            onClick = onNavigateToAdd,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                .semantics {
                    contentDescription = "navigateToAddCard"
                }
        ) {
            Text("Add a Card")
        }

        Button(
            onClick = onNavigateToSearch,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                .semantics {
                    contentDescription = "navigateToSearchCards"
                }
        ) {
            Text("Search Cards")
        }

        Button(
            onClick = onNavigateToLogIn,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                .semantics {
                    contentDescription = "navigateToLogIn"
                }
        ) {
            Text("Log in")
        }
        /*Button(
            modifier = Modifier.fillMaxWidth()
                .semantics { contentDescription = "ExecuteLogout" },
            onClick = {
                scope.launch {
                    appContext.dataStore.edit { preferences ->
                        preferences.remove(EMAIL)
                        preferences.remove(TOKEN)
                        onMessageChange(preferences[EMAIL] ?: "")
                    }
                }
            }
        ) {
            Text(
                "Log out",
                modifier = Modifier.semantics { contentDescription = "Logout" }
            )
        }*/
    }
}