package com.example.annamapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.annamapp.FlashCard
import com.example.annamapp.FlashCardDao
import com.example.annamapp.screens.AddCardScreen
import com.example.annamapp.screens.CardDetailScreen
import com.example.annamapp.screens.HomeScreen
import com.example.annamapp.screens.SearchScreen
import com.example.annamapp.screens.StudyScreen

@Composable
fun AppNavHost(
    userDao: FlashCardDao,
    navCtrller: NavHostController = rememberNavController(),
    modder: Modifier,      //i'm testing no default value for modifier param yet
    startDestnt: String = Routes.HOME,  //default start screen is home
    onMessageChange: (String) -> Unit = {}
) {
    // Define lambdas for database operations.
    // This is good practice as it keeps database logic out of the NavHost.
    val insertFlashCard: suspend (FlashCard) -> Unit = {
        userDao.insertAll(it) // or flashCard -> userDao.insertAll(flashCard)
    }
    val getAllCards: suspend () -> List<FlashCard> = {
        userDao.getAll()
    }
    val getCardById: suspend (Int) -> FlashCard? = {
        userDao.getCardById(it)
    }
    val deleteCard: suspend (FlashCard) -> Unit = {
        userDao.delete(it)
    }

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
            AddCardScreen(onMessageChange = onMessageChange, insertFlashCard = insertFlashCard)
        }

        // Updated composable for the Search/View All screen
        composable(Routes.SEARCH) {
            SearchScreen(
                getAllCards = getAllCards,
                onMessageChange = onMessageChange,
                onCardClick = {
                    // Navigate to the detail screen, passing the card's ID
                    navCtrller.navigate("${Routes.CARD_DETAIL}/$it") //or cardID -> navCtrller.navigate("${Routes.CARD_DETAIL}/$cardID")
                }
            )
        }

        composable(
            route = "${Routes.CARD_DETAIL}/{${Routes.CARD_ID_ARG}}", // e.g., "card_detail/5"
            arguments = listOf(navArgument(Routes.CARD_ID_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            // Extract the card ID from the navigation arguments
            val cardId = backStackEntry.arguments?.getInt(Routes.CARD_ID_ARG)
            if (cardId != null) {
                CardDetailScreen(
                    getCardById = getCardById,
                    deleteCard = deleteCard,
                    cardId = cardId,
                    onNavigateBack = { navCtrller.popBackStack() }, // Simple navigate back
                    onMessageChange = onMessageChange
                )
            } else {
                // Handle the case where ID is missing (shouldn't happen, but safe to have)
                navCtrller.popBackStack()
            }
        }

    }
}