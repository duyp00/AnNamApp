package com.example.annamapp.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.annamapp.room_sqlite_db.FlashCard
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * This screen now displays a list of all flashcards from the database.
 *
 * @param getAllCards A suspend function to fetch all cards.
 * @param deleteCard A suspend function to delete a card.
 * @param onEditClick A lambda function to handle navigation when 'Edit' is clicked.
 * @param onMessageChange A lambda to update the message in the bottom bar.
 */
@Composable
fun FlashCardList(
    cardList: List<FlashCard>,
    onEditClick: (Int) -> Unit,
    deleteCardClick: (FlashCard) -> Job,
    refreshList: () -> Job
) {
    // LazyColumn is efficient for displaying long (or short) lists.
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        refreshList() //fill the list when composing the LazyColumn
        // 'items' loops through all cards in cardList, creating a Card composable for each.
        items(items = cardList, key = { it.uid }) { card ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Color.LightGray)
                    .padding(8.dp), // Reduced padding slightly to fit buttons better
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // use weight(1f) so the text takes up remaining space before buttons
                Text(
                    text = "${card.englishCard ?: ""} = ${card.vietnameseCard ?: ""}",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )

                Row {
                    TextButton(onClick = { onEditClick(card.uid) }) {
                        Text("Edit")
                    }

                    TextButton(onClick = { deleteCardClick(card) }) {
                        Text("Del", color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchScreen(
    getAllCards: suspend () -> List<FlashCard>,
    deleteCard: suspend (FlashCard) -> Unit,
    onEditClick: (Int) -> Unit,
    onMessageChange: (String) -> Unit = {}
) {
    // This state will hold the list of cards retrieved from the database.
    var cardList by remember { mutableStateOf<List<FlashCard>>(emptyList()) }//can use listOf() instead, but memory-wise emptyList() is better
    val scope = rememberCoroutineScope()

    // Function to refresh the list
    val refreshList = {
        scope.launch {
            cardList = getAllCards()
        }
    }

    val deleteCardClick = { card: FlashCard ->
        scope.launch {
            deleteCard(card)
            onMessageChange("Card deleted")
            // Refresh the list after deletion
            refreshList()
        }
    }

    // LaunchedEffect runs this suspend block when the composable first appears.
    LaunchedEffect(key1 = Unit) {
        onMessageChange("Click Edit to modify, Delete to remove")
        //refreshList() //either here or in the LazyColumn
    }

    FlashCardList(
        cardList = cardList,
        onEditClick = onEditClick,
        deleteCardClick = deleteCardClick,
        refreshList = refreshList,
    )
}