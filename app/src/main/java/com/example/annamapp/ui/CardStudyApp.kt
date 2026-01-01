package com.example.annamapp.ui

//import androidx.navigation.toRoute
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.annamapp.EMAIL
import com.example.annamapp.R
import com.example.annamapp.TOKEN
import com.example.annamapp.dataStore
import com.example.annamapp.navigation.AppNavHost
import com.example.annamapp.navigation.Routes
import com.example.annamapp.room_sqlite_db.FlashCardDao
import kotlinx.coroutines.launch

@Composable
fun CardStudyApp(
    flashCardDao: FlashCardDao,
    navController: NavHostController = rememberNavController(),
    networkService: NetworkService
) {
    //val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext

    // --- FIX: Use string-based routes for Scaffold logic ---

    // 1. Get the qualified name of the home route. This is our default.
    // e.g., "com.example.annamapp.navigation.Routes.Home"
    val homeRouteString = Routes.Home::class.qualifiedName

    // 2. Observe the nav back stack entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // 3. Get the current route as a STRING. Default to home.
    val currentRouteString = navBackStackEntry?.destination?.route

    // 4. Compare strings to determine if the back button should be shown.
    val showBack = currentRouteString != homeRouteString

    // --- END FIX ---

    var message by rememberSaveable { mutableStateOf("") }

    // Top-level Scaffold
    Scaffold(
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 15.dp, start = 10.dp, top = 20.dp)
            ) {
                if (showBack) {
                    TextButton(
                        content = { Text(stringResource(R.string.back_button_label)) },
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.semantics { contentDescription = "navigateBack" },
                        border = BorderStroke(width = 1.dp, color = Color.Gray),
                        //colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        //    containerColor = Color.Transparent,
                        //    contentColor = MaterialTheme.colorScheme.primary,
                        //),
                    )
                }
                Text(
                    // 5. Pass the current route STRING to the title helper
                    text = titleForRoute(currentRouteString),
                    modifier = Modifier.weight(1f)
                        .semantics{contentDescription = "screen_title"},
                    style = MaterialTheme.typography.titleLarge
                )
                if (!showBack) {
                    Button(
                        modifier = Modifier.semantics { contentDescription = "ExecuteLogout" },
                        onClick = {
                            scope.launch {
                                appContext.dataStore.edit { preferences ->
                                    preferences.remove(EMAIL)
                                    preferences.remove(TOKEN)
                                    message = preferences[EMAIL] ?: ""
                                }
                            }
                        }
                    ) {
                        Text(
                            "Log out",
                            modifier = Modifier.semantics { contentDescription = "Logout" }
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomAppBar(modifier = Modifier.height(85.dp)) {
                Text(text = message, modifier = Modifier.semantics {
                    contentDescription = "Message"
                },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    ) { innerPadding ->
        AppNavHost(
            flashCardDao = flashCardDao,
            onMessageChange = {message = it},
            navCtrller = navController,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            networkService = networkService
        )
    }
}

// --- FIX: Update helper function to work with route strings ---
@Composable
fun titleForRoute(route: String?): String {
    // Compare the route string against the qualified name of each route class
    return when {
        route == Routes.Home::class.qualifiedName -> "Home"
        route == Routes.Study::class.qualifiedName -> "Study Cards"
        route == Routes.Add::class.qualifiedName -> "Add a Card"
        route == Routes.Search::class.qualifiedName -> "Search Cards"
        route == Routes.LogIn::class.qualifiedName -> "Log In"
        route == Routes.TokenScreen::class.qualifiedName -> "Enter Received Token"

        // For routes with arguments (like CardDetail), the route string will be
        // "com.example...Routes.CardDetail/{cardId}"
        // So we check if the string *starts with* the class name.
        route?.startsWith(Routes.SearchResults::class.qualifiedName!!) == true -> "Search Results"
        route?.startsWith(Routes.CardDetail::class.qualifiedName!!) == true -> "Card Details"

        else -> "New screen"
    }
}
// --- END FIX ---