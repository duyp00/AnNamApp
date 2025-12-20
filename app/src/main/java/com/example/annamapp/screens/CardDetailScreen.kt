package com.example.annamapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.annamapp.room_sqlite_db.FlashCard
import kotlinx.coroutines.launch

@Composable
fun CardDetailScreen(
    getCardById: suspend (Int?) -> FlashCard?,
    updateCard: suspend (FlashCard) -> Unit,
    //deleteCard: suspend (FlashCard) -> Unit,
    cardId: Int?,
    //onNavigateBack: () -> Unit,
    onMessageChange: (String) -> Unit
) {
    var card by remember { mutableStateOf<FlashCard?>(null) }

    var englishText by remember { mutableStateOf("") }
    var vietnameseText by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        card = getCardById(cardId)
        if (card != null) {
            englishText = card?.englishCard ?: ""
            vietnameseText = card?.vietnameseCard ?: ""
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
            /*
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

            Button(
                onClick = {
                    scope.launch {
                        // Create a new object with same ID but new properties
                        val updatedCard = card?.copy(
                            englishCard = englishText,
                            vietnameseCard = vietnameseText
                        ) ?: return@launch
                        updateCard(updatedCard)
                        onMessageChange("Card saved")
                        //onNavigateBack()
                    }
                }
            ) {
                Text("Save")
            }
        }
    }
}