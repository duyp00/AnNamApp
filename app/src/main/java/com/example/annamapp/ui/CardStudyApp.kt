package com.example.annamapp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.annamapp.FlashCardDao
import com.example.annamapp.R
import com.example.annamapp.navigation.AppNavHost
import com.example.annamapp.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class) // Required for TopAppBar
@Composable
fun CardStudyApp(
    userDao: FlashCardDao
) {
    val navController = rememberNavController()

    // observe current route to know when to show the Back button
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME
    val showBack = currentRoute != Routes.HOME
    var message by rememberSaveable { mutableStateOf("") }

    // Top-level Scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titleForRoute(currentRoute),
                        //unique test tag to avoid ambiguity during testing
                        modifier = Modifier.testTag("screen_title")
                    )
                },
                navigationIcon = if (showBack) {
                    {
                        TextButton(
                            onClick = { navController.navigateUp() },
                            //unique test tag to avoid ambiguity during testing
                            modifier = Modifier.testTag("back_button"),
                            content = { Text(stringResource(R.string.back_button_label)) }
                        )// or trailing lambda syntax for last param
                    }
                } else { {} /*Provide an empty lambda if not showing the icon*/}
            )
        },
        bottomBar = {
            BottomAppBar {
                Text(text = message)
            }
        }
    ) { innerPadding ->
        // Place the navHost inside the Scaffold content area
        AppNavHost(
            userDao = userDao,
            onMessageChange = {message = it},
            navCtrller = navController,
            startDestnt = Routes.HOME,
            modder = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

//@Composable
private fun titleForRoute(route: String) = when (route) {
    Routes.HOME -> "Home"
    Routes.STUDY -> "Study Cards"
    Routes.ADD -> "Add a Card"
    Routes.SEARCH -> "Search Cards"
    Routes.CARD_DETAIL -> "Card Details"
    else -> "New screen"
}

@Preview(/*showBackground = true*/)
@Composable
fun PreviewApp() {
    // It's a good practice to wrap previews inside theme
    // M3 theme {
    //CardStudyApp()
    // }
}