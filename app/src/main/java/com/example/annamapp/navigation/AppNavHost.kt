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
import com.example.annamapp.screens.LogInScreen
import com.example.annamapp.screens.SearchScreen
import com.example.annamapp.screens.StudyScreen
import com.example.annamapp.screens.SearchResultScreen
import com.example.annamapp.ui.NetworkService
//import com.example.annamapp.navigation.Routes.*

@Composable
fun AppNavHost(
    flashCardDao: FlashCardDao,
    navCtrller: NavHostController = rememberNavController(),
    modifier: Modifier,
    onMessageChange: (String) -> Unit = {},
    networkService: NetworkService
) {
    // Define lambdas for database operations.
    val insertFlashCard: suspend (FlashCard) -> Unit = {
        flashCardDao.insertAll(it)
    }
    val updateFlashCard: suspend (FlashCard) -> Unit = {
        flashCardDao.updateCard(it)
    }
    /*val getAllCards: suspend () -> List<FlashCard> = {
        flashCardDao.getAll()
    }*/
    val getCardById: suspend (Int?) -> FlashCard? = {
        flashCardDao.getCardById(it)
    }
    val deleteCard: suspend (FlashCard) -> Unit = {
        flashCardDao.delete(it)
    }
    val findByWord: suspend (String, String) -> FlashCard? = { en, vn ->
        flashCardDao.findByCards(english = en, vietnamese = vn)
    }

    // The startDestination parameter now takes the Routes.Home type-safe object directly
    NavHost(navController = navCtrller, startDestination = Routes.Home, modifier = modifier) {

        // Use composable<T> for type-safe destinations without arguments
        composable<Routes.Home> {
            HomeScreen(
                // Navigate by passing the destination object
                onNavigateToStudy = { navCtrller.navigate(Routes.Study) },
                onNavigateToAdd = { navCtrller.navigate(Routes.Add) },
                onNavigateToSearch = { navCtrller.navigate(Routes.Search) },
                onNavigateToLogIn = { navCtrller.navigate(Routes.LogIn) },
                onMessageChange = onMessageChange
            )
        }

        composable<Routes.Study> {
            StudyScreen(onMessageChange = onMessageChange)
        }

        composable<Routes.Add> {
            AddCardScreen(onMessageChange = onMessageChange, insertFlashCard = insertFlashCard, findByWord = findByWord)
        }

        composable<Routes.Search> {
            SearchScreen(
                onSearch = { filters -> navCtrller.navigate(filters) },
                onShowAllCards = { navCtrller.navigate(Routes.SearchResults()) },
                onMessageChange = onMessageChange
            )
        }

        composable<Routes.LogIn> {
            LogInScreen(onMessageChange = onMessageChange, networkService = networkService)
        }

        composable<Routes.SearchResults> { backStackEntry ->
            val args = backStackEntry.toRoute<Routes.SearchResults>()
            SearchResultScreen(
                filters = args,
                performSearch = { filters ->
                    searchFlashCards(flashCardDao, filters)
                },
                deleteCards = { deletelist ->
                    deletelist.forEach { card -> deleteCard(card) }
                },
                onNavigateToCard = { cardId -> navCtrller.navigate(Routes.CardDetail(cardId)) },
                onMessageChange = onMessageChange
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

private suspend fun searchFlashCards(
    flashCardDao: FlashCardDao,
    filters: Routes.SearchResults
): List<FlashCard> {
    return if (filters.englishEnabled || filters.vietnameseEnabled) {
        flashCardDao.searchCards(
            englishQuery = filters.englishQuery,
            englishEnabled = filters.englishEnabled,
            englishWholeWord = filters.englishWholeWord,
            vietnameseQuery = filters.vietnameseQuery,
            vietnameseEnabled = filters.vietnameseEnabled,
            vietnameseWholeWord = filters.vietnameseWholeWord
        )
    } else {
        flashCardDao.getAll()
    }
}
