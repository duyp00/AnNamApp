package com.example.annamapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.annamapp.FlashCard
import kotlinx.coroutines.launch

/**
 * This is the new screen to show card details and allow deletion.
 *
 * @param getCardById A suspend function to get a single card by its ID.
 * @param deleteCard A suspend function to delete a card.
 * @param cardId The unique ID of the card to display.
 * @param onNavigateBack A lambda function to navigate back after deletion.
 * @param onMessageChange A lambda to update the message in the bottom bar.
 */
@Composable
fun CardDetailScreen(
    getCardById: suspend (Int) -> FlashCard?,
    deleteCard: suspend (FlashCard) -> Unit,
    cardId: Int,
    onNavigateBack: () -> Unit,
    onMessageChange: (String) -> Unit
) {
    // This state will hold the specific card we're looking at.
    var card by remember { mutableStateOf<FlashCard?>(null) }
    // Coroutine scope to run the delete operation.
    val scope = rememberCoroutineScope()

    // This effect runs when 'cardId' changes, or on first load (key1 = cardId). It fetches the specific card from the database.
    //CardDetailScreen is launched via route navigation, no need to watch cardId changes? (just key1 = Unit instead?)
    LaunchedEffect(key1 = Unit) {
        card = getCardById(cardId)
        if (card != null) {
            onMessageChange("Card details")
        } else {
            onMessageChange("Card not found")
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Only display the content if the card has been successfully loaded.
        card?.let {
            OutlinedTextField(
                value = it.englishCard ?: "",
                onValueChange = {},
                label = { Text("en") },
                modifier = Modifier.fillMaxWidth(),
                //readOnly = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = it.vietnameseCard ?: "",
                onValueChange = {},
                label = { Text("vn") },
                modifier = Modifier.fillMaxWidth(),
                //readOnly = true
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = {
                // Launch a coroutine to delete the card
                scope.launch {
                    deleteCard(it) // 'it' refers to the non-null 'card'
                    onNavigateBack() // Go back to the list screen
                }
            }) {
                Text("Delete")
            }
        } ?: run {
            // Show a loading message while the card is being fetched. optional
            if (card == null) {
                Text("Loading card details...")
            }
        }
    }
}