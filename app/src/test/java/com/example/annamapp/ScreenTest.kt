package com.example.annamapp

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.annamapp.navigation.Routes
import com.example.annamapp.room_sqlite_db.FlashCard
import com.example.annamapp.room_sqlite_db.FlashCardDao
import com.example.annamapp.ui.CardStudyApp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

class DummyFlashCardDao : FlashCardDao {
    override suspend fun getAll(): List<FlashCard> {
        return emptyList<FlashCard>()
    }

    override suspend fun loadAllByIds(flashCardIds: IntArray): List<FlashCard> {
        TODO("Not yet implemented")
    }

    override suspend fun getCardById(uid: Int?): FlashCard? {
        TODO("Not yet implemented")
    }


    override suspend fun findByCards(
        english: String,
        vietnamese: String
    ): FlashCard {
        return FlashCard(0, "", "")
    }

    override suspend fun searchCards(
        englishQuery: String,
        englishEnabled: Boolean,
        englishWholeWord: Boolean,
        vietnameseQuery: String,
        vietnameseEnabled: Boolean,
        vietnameseWholeWord: Boolean
    ): List<FlashCard> {
        TODO("Not yet implemented")
    }

    override suspend fun insertAll(vararg flashCard: FlashCard) {
    }

    override suspend fun delete(flashCard: FlashCard) {
        TODO("Not yet implemented")
    }

    override suspend fun updateCard(flashCard: FlashCard) {
        TODO("Not yet implemented")
    }

    suspend fun updateCard(
        englishOld: String,
        vietnameseOld: String,
        englishNew: String,
        vietnameseNew: String
    ) {

    }

    suspend fun delete(english: String, vietnamese: String) {
    }


}

class DummyFlashCardDaoUnsuccessfulInsert : FlashCardDao {
    override suspend fun getAll(): List<FlashCard> {
        return emptyList<FlashCard>()
    }

    override suspend fun loadAllByIds(flashCardIds: IntArray): List<FlashCard> {
        TODO("Not yet implemented")
    }

    override suspend fun getCardById(uid: Int?): FlashCard? {
        TODO("Not yet implemented")
    }


    override suspend fun findByCards(
        english: String,
        vietnamese: String
    ): FlashCard {
        return FlashCard(0, "", "")
    }

    override suspend fun searchCards(
        englishQuery: String,
        englishEnabled: Boolean,
        englishWholeWord: Boolean,
        vietnameseQuery: String,
        vietnameseEnabled: Boolean,
        vietnameseWholeWord: Boolean
    ): List<FlashCard> {
        TODO("Not yet implemented")
    }

    override suspend fun insertAll(vararg flashCard: FlashCard) {
        throw SQLiteConstraintException()
    }

    override suspend fun delete(flashCard: FlashCard) {
        TODO("Not yet implemented")
    }

    override suspend fun updateCard(flashCard: FlashCard) {
        TODO("Not yet implemented")
    }

    suspend fun updateCard(
        englishOld: String,
        vietnameseOld: String,
        englishNew: String,
        vietnameseNew: String
    ) {

    }

    suspend fun delete(english: String, vietnamese: String) {
    }
}

@RunWith(RobolectricTestRunner::class)
class ScreenTest {
    //This creates a shell activity that doesn't have any pre-set content, allowing your tests to call setContent() to set up the UI for the test.
    @get:Rule
    val composeTestRule = createComposeRule()

    // CardStudyApp
    // type: Navigation
    @Test
    fun homeStartDestination() {
        val navController =
            TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        val dummyFlashCardDao = DummyFlashCardDao()
        composeTestRule.setContent {
            CardStudyApp(
                navController = navController,
                flashCardDao = dummyFlashCardDao
            )
        }
        assertEquals(true, navController.currentDestination?.hasRoute<Routes.Home>())
    }


    @Test
    fun clickOnStudyCards() {
        val navController =
            TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        val dummyFlashCardDao = DummyFlashCardDao()
        composeTestRule.setContent {
            CardStudyApp(
                navController = navController,
                flashCardDao = dummyFlashCardDao
            )
        }
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Home)
        }
        composeTestRule.onNodeWithContentDescription("navigateToStudyCards")
            .assertExists()
            //.assertTextEquals("Study Cards")
            .performClick();
        assertEquals(true, navController.currentDestination?.hasRoute<Routes.Study>())
    }

    @Test
    fun clickOnAddCard() {
        val navController =
            TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        val dummyFlashCardDao = DummyFlashCardDao()

        composeTestRule.setContent {
            CardStudyApp(
                navController = navController,
                flashCardDao = dummyFlashCardDao
            )
        }
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Home)
        }
        composeTestRule.onNodeWithContentDescription("navigateToAddCard")
            .assertExists()
            //.assertTextEquals("Add a Card")
            .performClick();
        assertEquals(true, navController.currentDestination?.hasRoute<Routes.Add>())
    }

    @Test
    fun clickOnSearchCards() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        val dummyFlashCardDao = DummyFlashCardDao()

        composeTestRule.setContent {
            CardStudyApp(
                navController = navController,
                flashCardDao = dummyFlashCardDao
            )
        }
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Home)
        }
        composeTestRule.onNodeWithContentDescription("navigateToSearchCards")
            .assertExists()
            //.assertTextEquals("Search Cards")
            .performClick();
        assertEquals(true, navController.currentDestination?.hasRoute<Routes.Search>())
    }


    @Test
    fun homeScreenRetained_afterConfigChange() {
        val stateRestorationTester = StateRestorationTester(composeTestRule)
        /*
        The StateRestorationTester class is used to test the state restoration for composable components without recreating activities.
        This makes tests faster and more reliable, as activity recreation is a complex process with multiple synchronization mechanisms:
        */
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())

        val dummyFlashCardDao = DummyFlashCardDao()

        // Set content through the StateRestorationTester object.
        stateRestorationTester.setContent {
            CardStudyApp(
                navController = navController,
                flashCardDao = dummyFlashCardDao
            )
        }
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Home)
        }
        // Simulate a config change.
        stateRestorationTester.emulateSavedInstanceStateRestore()
        assertEquals(true, navController.currentDestination?.hasRoute<Routes.Home>())
    }

// AddCardScren
// type: Logic


    // AddCard
// type: navigation-back
    @Test
    fun clickOnAddCardAndBack() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())

        val dummyFlashCardDao = DummyFlashCardDao()
        composeTestRule.setContent {
            CardStudyApp(
                navController = navController,
                flashCardDao = dummyFlashCardDao
            )
        }
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Home)
        }
        composeTestRule.onNodeWithContentDescription("navigateToAddCard")
            .performClick();

        composeTestRule.onNodeWithContentDescription("navigateBack")
            .assertExists()
            .performClick();
        assertEquals(true, navController.currentDestination?.hasRoute<Routes.Home>())
    }

    // AddCard
    @Test
    fun typeOnEnTextInput() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        val dummyFlashCardDao = DummyFlashCardDao()

        composeTestRule.setContent {
            CardStudyApp(
                navController = navController,
                flashCardDao = dummyFlashCardDao
            )
        }
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Home)
        }
        composeTestRule.onNodeWithContentDescription("navigateToAddCard")
            .performClick();

        val textInput = "house"
        composeTestRule.onNodeWithContentDescription("enTextField").assertExists()
            .performTextInput(textInput)
        composeTestRule.onNodeWithContentDescription("enTextField")
            .assertTextEquals("english", textInput)
    }

    // AddCard
    @Test
    fun keepEnglishStringAfterRotation() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        val dummyFlashCardDao = DummyFlashCardDao()

        val stateRestorationTester = StateRestorationTester(composeTestRule)
        stateRestorationTester.setContent {
            CardStudyApp(
                navController = navController,
                flashCardDao = dummyFlashCardDao
            )
        }
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Home)
        }
        composeTestRule.onNodeWithContentDescription("navigateToAddCard")
            .performClick();

        val textInput = "house"
        composeTestRule.onNodeWithContentDescription("enTextField").assertExists()
            .performTextInput(textInput)

        // Simulate a config change.
        stateRestorationTester.emulateSavedInstanceStateRestore()
        composeTestRule.onNodeWithContentDescription("enTextField")
            .assertTextEquals("english", textInput)
    }

    @Test
    fun clickOnAddCardSuccessful() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        val dummyFlashCardDao = DummyFlashCardDao()
        composeTestRule.setContent {
            CardStudyApp(
                navController = navController,
                flashCardDao = dummyFlashCardDao
            )
        }
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Home)
        }
        composeTestRule.onNodeWithContentDescription("navigateToAddCard")
            .performClick();

        composeTestRule.onNodeWithContentDescription("add_card_button")
            .assertExists()
            .performClick()

        composeTestRule.onNodeWithContentDescription("Message")
            .assertExists()
            .assert(hasText(text = "Added card:", substring = true))
            //.assertTextContains("Added card:")
    }


// AddCardScren
// type: Logic

    @Test
    fun clickOnAddCardUnSuccessful() {

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        val dummyFlashCardDao = DummyFlashCardDaoUnsuccessfulInsert()

        composeTestRule.setContent {
            CardStudyApp(
                navController = navController,
                flashCardDao = dummyFlashCardDao
            )
        }
        composeTestRule.runOnUiThread {
            navController.navigate(Routes.Home)
        }
        composeTestRule.onNodeWithContentDescription("navigateToAddCard")
            .performClick();

        composeTestRule.onNodeWithContentDescription("add_card_button")
            .assertExists()
            .performClick()

        composeTestRule.onNodeWithContentDescription("Message")
            .assertExists()
            .assertTextEquals("Card already exists in database.")
    }
}
