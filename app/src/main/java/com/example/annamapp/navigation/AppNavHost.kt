package com.example.annamapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
    modder: Modifier,
    // The start destination is now a type-safe object, not a String
    startDestnt: Routes, //= Routes.Home,
    onMessageChange: (String) -> Unit = {}
) {
    // Define lambdas for database operations. (This part remains the same)
    val insertFlashCard: suspend (FlashCard) -> Unit = {
        userDao.insertCard(it)
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

    // The startDestination parameter now takes the Routes.Home object directly
    NavHost(navController = navCtrller, startDestination = startDestnt, modifier = modder) {

        // Use composable<T> for type-safe destinations without arguments
        composable<Routes.Home> {
            HomeScreen(
                // Navigate by passing the destination object
                onNavigateToStudy = { navCtrller.navigate(Routes.Study) },
                onNavigateToAdd = { navCtrller.navigate(Routes.Add) },
                onNavigateToSearch = { navCtrller.navigate(Routes.Search) },
                onMessageChange = onMessageChange
            )
        }

        composable<Routes.Study> {
            StudyScreen(onMessageChange = onMessageChange)
        }

        composable<Routes.Add> {
            AddCardScreen(onMessageChange = onMessageChange, insertFlashCard = insertFlashCard)
        }

        composable<Routes.Search> {
            SearchScreen(
                getAllCards = getAllCards,
                onMessageChange = onMessageChange,
                onCardClick = { cardId ->
                    // Navigate with arguments by passing an instance of the data class
                    navCtrller.navigate(Routes.CardDetail(cardId = cardId))
                }
            )
        }

        // Use composable<T> for destinations WITH arguments
        // No need to define "route" strings or "arguments" lists anymore!
        composable<Routes.CardDetail> { backStackEntry ->
            // Retrieve the type-safe arguments object
            // No more manual parsing of backStackEntry.arguments!
            val args = backStackEntry.toRoute<Routes.CardDetail>()

            CardDetailScreen(
                getCardById = getCardById,
                deleteCard = deleteCard,
                cardId = args.cardId, // Access arguments directly
                onNavigateBack = { navCtrller.popBackStack() },
                onMessageChange = onMessageChange
            )
            // The 'else' case for a missing ID is no longer needed,
            // as navigation will fail at compile time if you don't provide a cardId.
        }
    }
}