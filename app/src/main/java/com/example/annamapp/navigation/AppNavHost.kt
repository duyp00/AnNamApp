package com.example.annamapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.annamapp.screens.AddCardScreen
import com.example.annamapp.screens.HomeScreen
import com.example.annamapp.screens.SearchScreen
import com.example.annamapp.screens.StudyScreen

@Composable
fun AppNavHost(
    navCtrller: NavHostController = rememberNavController(),
    modder: Modifier,      //i'm testing no default value for modifier param yet
    startDestnt: String = Routes.HOME,  //default start screen is home
    onMessageChange: (String) -> Unit = {}
) {
    NavHost(navController = navCtrller, startDestination = startDestnt, modifier = modder) {
        composable(startDestnt) {
            HomeScreen(
                onNavigateToStudy = { navCtrller.navigate(Routes.STUDY) },
                onNavigateToAdd = { navCtrller.navigate(Routes.ADD) },
                onNavigateToSearch = { navCtrller.navigate(Routes.SEARCH) },
                onMessageChange = onMessageChange
            )
        }

        composable(Routes.STUDY) {
            StudyScreen(onMessageChange = onMessageChange)
        }

        composable(Routes.ADD) {
            AddCardScreen(onMessageChange = onMessageChange)
        }

        composable(Routes.SEARCH) {
            SearchScreen(onMessageChange = onMessageChange)
        }
    }
}