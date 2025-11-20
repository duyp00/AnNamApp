package com.example.annamapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.annamapp.room_sqlite_db.FlashCard
import com.example.annamapp.room_sqlite_db.FlashCardDao
import com.example.annamapp.screens.AddCardScreen
import com.example.annamapp.screens.CardDetailScreen
import com.example.annamapp.screens.HomeScreen
import com.example.annamapp.screens.SearchScreen
import com.example.annamapp.screens.StudyScreen

@Composable
fun AppNavHost(
    flashCardDao: FlashCardDao,
    navCtrller: NavHostController = rememberNavController(),
    modder: Modifier,
    // The start destination is now a type-safe object, not a String
    startDestnt: Routes, //= Routes.Home,
    onMessageChange: (String) -> Unit = {}
) {
    // Define lambdas for database operations.
    val insertFlashCard: suspend (FlashCard) -> Unit = {
        flashCardDao.insertAll(it)
    }
    val updateFlashCard: suspend (FlashCard) -> Unit = {
        flashCardDao.updateCard(it)
    }
    val getAllCards: suspend () -> List<FlashCard> = {
        flashCardDao.getAll()
    }
    val getCardById: suspend (Int?) -> FlashCard? = {
        flashCardDao.getCardById(it)
    }
    val deleteCard: suspend (FlashCard) -> Unit = {
        flashCardDao.delete(it)
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
                deleteCard = deleteCard,
                onMessageChange = onMessageChange,
                onEditClick = { cardId ->
                    // Navigate with arguments by passing an instance of the data class
                    navCtrller.navigate(Routes.CardDetail(cardId = cardId))
                }
            )
        }

        // Use composable<T> for destinations WITH arguments
        composable<Routes.CardDetail> { backStackEntry ->
            // Retrieve the type-safe arguments object
            val args = backStackEntry.toRoute<Routes.CardDetail>()

            CardDetailScreen(
                getCardById = getCardById,
                updateCard = updateFlashCard,
                //deleteCard = deleteCard,
                cardId = args.cardId, // Access arguments directly
                //onNavigateBack = { navCtrller.popBackStack() },
                onMessageChange = onMessageChange
            )
        }
    }
}