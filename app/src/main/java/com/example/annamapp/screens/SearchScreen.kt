package com.example.annamapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.annamapp.FlashCard

/**
 * This screen now displays a list of all flashcards from the database.
 *
 * @param getAllCards A suspend function to fetch all cards.
 * @param onCardClick A lambda function to handle navigation when a card is clicked.
 * @param onMessageChange A lambda to update the message in the bottom bar.
 */
@Composable
fun SearchScreen(
    getAllCards: suspend () -> List<FlashCard>,
    onCardClick: (Int) -> Unit,
    onMessageChange: (String) -> Unit = {}
) {
    // This state will hold the list of cards retrieved from the database.
    var cardList by remember { mutableStateOf<List<FlashCard>>(emptyList()) }

    // LaunchedEffect runs this suspend block when the composable first appears.
    // It will fetch all cards and update the cardList state.
    // The key 'Unit' means this effect runs only once.
    LaunchedEffect(key1 = Unit) {
        onMessageChange("Click on a card to see details")
        cardList = getAllCards()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // LazyColumn is efficient for displaying long (or short) lists.
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // 'items' loops through our cardList
            items(cardList, key = { it.uid }) { card ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            // When clicked, navigate to the detail screen using the card's unique ID.
                            onCardClick(card.uid)
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Display the card content as shown in the video (e.g., "test1 = test2")
                        Text(
                            text = "${card.englishCard ?: ""} = ${card.vietnameseCard ?: ""}"
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSearchScreen() {
    // A preview showing what the list might look like
    val previewCards = listOf(
        FlashCard(0, "test1", "test2"),
        FlashCard(1, "test3", "test4")
    )
    SearchScreen(
        getAllCards = { previewCards },
        onCardClick = {}
    )
}