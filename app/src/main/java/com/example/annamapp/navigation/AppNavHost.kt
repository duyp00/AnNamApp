package com.example.annamapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.annamapp.screens.SearchResultScreen
import com.example.annamapp.screens.SearchScreen
import com.example.annamapp.screens.StudyScreen
import com.example.annamapp.screens.loadAudioFileFromDiskForText
import com.example.annamapp.ui.NetworkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
//import com.example.annamapp.navigation.Routes.*

@Composable
fun AppNavHost(
    flashCardDao: FlashCardDao,
    navCtrller: NavHostController = rememberNavController(),
    modifier: Modifier,
    onMessageChange: (String) -> Unit = {},
    networkService: NetworkService
) {
    val appContext = LocalContext.current.applicationContext
    // Define lambda notions for database operations
    val insertFlashCard: suspend (FlashCard) -> Unit = {
        flashCardDao.insertAll(it)
    }
    val updateFlashCard: suspend (FlashCard, String, String) -> Unit = {
    flashcard, enInitial, vnInitial ->
        flashCardDao.updateCard(flashcard)
        if (enInitial != flashcard.englishCard) {
            val oldAudioFile = loadAudioFileFromDiskForText(
                appContext = appContext,
                text = enInitial,
                language = "en"
            ).file
            withContext(Dispatchers.IO) { oldAudioFile.delete() }
        }
        if (vnInitial != flashcard.vietnameseCard) {
            val oldAudioFile = loadAudioFileFromDiskForText(
                appContext = appContext,
                text = vnInitial,
                language = "vi"
            ).file
            withContext(Dispatchers.IO) { oldAudioFile.delete() }
        }
    }
    //val getCardById: suspend (Int) -> FlashCard? = {
    //    flashCardDao.getCardById(it)
    //}
    val deleteCard: suspend (FlashCard) -> Unit = {
        flashCardDao.delete(it)
    }
    val findByWord: suspend (String, String) -> FlashCard? = { en, vn ->
        flashCardDao.findByCards(english = en, vietnamese = vn)
    }
    val pickCardLesson: suspend (Int) -> List<FlashCard> = {
        flashCardDao.getLesson(it)
    }
    val searchFlashCards: suspend (Routes.SearchResults) -> List<FlashCard> = { filters ->
        if (filters.englishEnabled || filters.vietnameseEnabled) {
            flashCardDao.searchCards(
                englishQuery = filters.englishQuery,
                englishEnabled = filters.englishEnabled,
                englishWholeWord = filters.englishWholeWord,
                vietnameseQuery = filters.vietnameseQuery,
                vietnameseEnabled = filters.vietnameseEnabled,
                vietnameseWholeWord = filters.vietnameseWholeWord
            )
        } else { flashCardDao.getAll() }
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
            StudyScreen(
                onMessageChange = onMessageChange,
                pickCardLesson = pickCardLesson,
                networkService = networkService
            )
        }

        composable<Routes.Add> {
            AddCardScreen(
                onMessageChange = onMessageChange,
                insertFlashCard = insertFlashCard,
                findByWord = findByWord
            )
        }

        composable<Routes.Search> {
            SearchScreen(
                onSearch = { filters -> navCtrller.navigate(filters) },
                onShowAllCards = { filters -> navCtrller.navigate(filters) },
                onMessageChange = onMessageChange
            )
        }

        composable<Routes.LogIn> {
            LogInScreen(
                onMessageChange = onMessageChange,
                //networkService = networkService,
                onNavigateHome = { navCtrller.navigate(Routes.Home) }
            )
        }

        composable<Routes.SearchResults> { backStackEntry ->
            val args = backStackEntry.toRoute<Routes.SearchResults>()
            SearchResultScreen(
                filters = args,
                performSearch = searchFlashCards,
                deleteCards = { deletelist ->
                    deletelist.forEach { card ->
                        val enFile = loadAudioFileFromDiskForText(
                            appContext = appContext,
                            text = card.englishCard.orEmpty(),
                            language = "en"
                        ).file
                        val viFile = loadAudioFileFromDiskForText(
                            appContext = appContext,
                            text = card.vietnameseCard.orEmpty(),
                            language = "vi"
                        ).file
                        withContext(Dispatchers.IO) {
                            enFile.delete()
                            viFile.delete()
                        }
                        deleteCard(card)
                    }
                },
                onNavigateToCard = { en, vn -> navCtrller.navigate(Routes.CardDetail(en, vn)) },
                onMessageChange = onMessageChange
            )
        }

        // Use composable<T> for destinations WITH arguments
        composable<Routes.CardDetail> { backStackEntry ->
            // Retrieve the type-safe arguments object
            val args = backStackEntry.toRoute<Routes.CardDetail>()
            CardDetailScreen(
                enWord = args.en,
                vnWord = args.vn,
                updateCard = updateFlashCard,
                onMessageChange = onMessageChange,
                findByWord = findByWord,
                networkService = networkService,
                /*onNavigateBack = { navCtrller.popBackStack() },*/
                /*getCardById = getCardById,*/ /*deleteCard = deleteCard,*/ /*cardId = args.cardId,*/
            )
        }
    }
}