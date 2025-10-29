package com.example.annamapp

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.annamapp.navigation.AppNavHost
import com.example.annamapp.navigation.Routes
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test

class AppNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun launchHomeAtStart() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())

        composeTestRule.setContent {
            AppNavHost(
                navCtrller = navController,
                startDestnt = Routes.HOME,
                modder = Modifier,
            )
        }

        assertEquals("home", navController.currentDestination?.route)
        //should make a separate function to test Add Card screen navigation, below is just to see if it works
        composeTestRule.onNodeWithContentDescription("openAddCardScreen").performClick()
        assertEquals("add", navController.currentDestination?.route)
    }

    /*
    @Test
    fun testNavigationAndInteraction() {
        // SET CONTENT
        composeTestRule.setContent {
            CardStudyApp()
        }

        // FIND & VERIFY home screen is visible
        // We can use text here since it's unique on the home screen
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()

        composeTestRule.onNodeWithText("Add a Card").performClick()

        // VERIFY THE RESULT using the unique testTag for the title
        // This is now unambiguous and clearly states we are checking the screen title.
        composeTestRule.onNodeWithTag("screen_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("add_screen_header").assertIsDisplayed()

        // Type "abc" in Question and Answer text fields.
        composeTestRule.onNodeWithTag("question_tag")
            .performTextInput("abc")
        composeTestRule.onNodeWithTag("answer_tag")
            .performTextInput("abc")

        // Verify "abc" is displayed in the fields.
        composeTestRule.onNodeWithTag("question_tag").assert(hasText("abc", substring = true))
        composeTestRule.onNodeWithTag("answer_tag").assert(hasText("abc", substring = true))

        // Re-type text and then click "Clear" button.
        composeTestRule.onNodeWithTag("question_tag").performTextInput("test clear")
        composeTestRule.onNodeWithTag("answer_tag").performTextInput("test clear")
        composeTestRule.onNodeWithText("Clear").performClick()

        // Verify text is cleared.
        composeTestRule.onNodeWithTag("question_tag").assert(hasText("", substring = true))
        composeTestRule.onNodeWithTag("answer_tag").assert(hasText("", substring = true))

        composeTestRule.onNodeWithText("Back").performClick()
    }
     */
}
