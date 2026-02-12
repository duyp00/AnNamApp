package com.example.annamapp.ui

//import androidx.navigation.toRoute
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.annamapp.networking.NetworkService
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

    // 1. Observe the nav back stack entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    // 2. Get the qualified name of the home route.
    // e.g., "com.example.annamapp.navigation.Routes.Home"
    val homeRouteString = Routes.Home::class.qualifiedName
    // 3. Get the current route as a STRING.
    val currentRouteString = navBackStackEntry?.destination?.route

    var message by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            // 4. Compare strings to determine if the back button should be shown.
            val showBack = currentRouteString != homeRouteString
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 15.dp, start = 10.dp, top = 20.dp)
            ) {
                if (showBack) {
                    TextButton(
                        content = { Text(stringResource(R.string.back_button_label)) },
                        onClick = { navController.navigateUp() }, //or popBackStack(), but if click too fast it pop the NavHost
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
                Box(
                    modifier = Modifier//.height(85.dp)
                        .verticalScroll(rememberScrollState())//.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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

fun titleForRoute(route: String?): String {
    return when {
        route == Routes.Home::class.qualifiedName -> "Home"
        route == Routes.Study::class.qualifiedName -> "Study Cards"
        route == Routes.Add::class.qualifiedName -> "Add a Card"
        route == Routes.Search::class.qualifiedName -> "Search Cards"
        route == Routes.LogIn::class.qualifiedName -> "Log In"

        //for routes with arguments (like CardDetail), the route string will be
        //"com.example...Routes.CardDetail/{cardId}"
        //so check if the string *starts with* the class name.
        //non-null assert is used because it's always not null in this case
        route?.startsWith(Routes.SearchResults::class.qualifiedName!!) ?: false -> "Search Results"
        route?.startsWith(Routes.CardDetail::class.qualifiedName!!) ?: false -> "Card Details"

        else -> "New screen"
    }
}

/*
Bottom navigation how-to:
1. Define Destinations: Create a sealed class or data objects to define your app's screens and their corresponding routes in a type-safe manner.
sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Home : Screen("home", Icons.Default.Home, "Home")
    object Profile : Screen("profile", Icons.Default.Person, "Profile")
    object Settings : Screen("settings", Icons.Default.Settings, "Settings")
}
val items = listOf(Screen.Home, Screen.Profile, Screen.Settings)

2. Set up NavController and NavHost: In your main activity or a central navigation file, create the NavController and the NavHost within a Scaffold. The Scaffold allows you to position the NavigationBar at the bottom and the NavHost in the main content area.
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { /* Home Screen Composable */ }
            composable(Screen.Profile.route) { /* Profile Screen Composable */ }
            composable(Screen.Settings.route) { /* Settings Screen Composable */ }
        }
    }
}

3. Implement the Navigation Bar: Create the BottomNavigationBar composable, which uses NavigationBarItem to handle user clicks and navigate between destinations.
@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true // Save the state of the popped destinations
                        }
                        // Avoid multiple copies of the same destination when reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
*/