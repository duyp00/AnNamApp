package com.example.annamapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.example.annamapp.room_sqlite_db.FlashCard
import kotlinx.coroutines.launch

/**
 * This is the new screen to show card details, allow editing, and deletion.
 *
 * @param getCardById A suspend function to get a single card by its ID.
 * @param updateCard A suspend function to update the card.
 * @param deleteCard A suspend function to delete a card.
 * @param cardId The unique ID of the card to display.
 * @param onNavigateBack A lambda function to navigate back.
 * @param onMessageChange A lambda to update the message in the bottom bar.
 */
@Composable
fun CardDetailScreen(
    getCardById: suspend (Int) -> FlashCard?,
    updateCard: suspend (FlashCard) -> Unit,
    //deleteCard: suspend (FlashCard) -> Unit,
    cardId: Int,
    //onNavigateBack: () -> Unit,
    onMessageChange: (String) -> Unit
) {
    // This state will hold the specific card object from DB (for ID reference)
    var card by remember { mutableStateOf<FlashCard?>(null) }

    // Editable states for the text fields
    var englishText by remember { mutableStateOf("") }
    var vietnameseText by remember { mutableStateOf("") }

    // Coroutine scope to run operations.
    val scope = rememberCoroutineScope()

    // Fetch card data
    LaunchedEffect(key1 = Unit) {
        val fetchedCard = getCardById(cardId)
        card = fetchedCard
        if (fetchedCard != null) {
            englishText = fetchedCard.englishCard ?: ""
            vietnameseText = fetchedCard.vietnameseCard ?: ""
            onMessageChange("Edit card details")
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
        if (card != null) {
            OutlinedTextField(
                value = englishText,
                onValueChange = { englishText = it },
                label = { Text("English") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = vietnameseText,
                onValueChange = { vietnameseText = it },
                label = { Text("Vietnamese") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                /* // Delete Button
                Button(
                    onClick = {
                        scope.launch {
                            card?.let {
                                deleteCard(it)
                                onMessageChange("Card deleted")
                                onNavigateBack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
                Spacer(modifier = Modifier.width(16.dp))
                */

                // Save Button
                Button(
                    onClick = {
                        scope.launch {
                            // Create a copy of the current card with updated text
                            val updatedCard = card!!.copy(
                                englishCard = englishText,
                                vietnameseCard = vietnameseText
                            )
                            updateCard(updatedCard)
                            onMessageChange("Card saved")
                            //onNavigateBack()
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        } else {
            // Loading state
            Text("Loading card details...")
        }
    }
}