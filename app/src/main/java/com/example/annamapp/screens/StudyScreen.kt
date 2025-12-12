package com.example.annamapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.annamapp.room_sqlite_db.FlashCard
import kotlinx.coroutines.launch

@Composable
fun StudyScreen(
    onMessageChange: (String) -> Unit = {},
    pickCardLesson: suspend (Int) -> List<FlashCard>
) {
    val numberOfCardsToStudy = 3
    var cardList by remember { mutableStateOf<List<FlashCard>>(emptyList()) }
    var actualNumberofCardsFetched by remember { mutableStateOf(0) }
    var currentIndex by rememberSaveable { mutableStateOf(0) }
    var isVietnameseVisible by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun loadCards() {
        cardList = pickCardLesson(numberOfCardsToStudy)
        actualNumberofCardsFetched = cardList.size
        //currentIndex = 0
        //isVietnameseVisible = false
        if (actualNumberofCardsFetched == 0) {
            onMessageChange("No cards available for study.")
        } else {
            onMessageChange("Loaded $actualNumberofCardsFetched cards for study.")
        }
    }

    LaunchedEffect(Unit) {
        loadCards()
    }

    Column(
        //verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()/*.padding(24.dp)*/,
    ) {
        //this check is very important since cardList is being fetched asynchronously, or outOfBounds exception with cardList will be thrown
        if (actualNumberofCardsFetched == 0) {
            Text(
                text = "No cards to display",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            return@Column //see return@myLabel in Kotlin
        }

        val currentCard = cardList[currentIndex]
        val displayText = (if (isVietnameseVisible) currentCard.vietnameseCard else currentCard.englishCard) ?: ""
        val title = if (isVietnameseVisible) "Vietnamese" else "English"

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = displayText,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.clickable {
                    isVietnameseVisible = !isVietnameseVisible
                }
                .padding(12.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (isVietnameseVisible) {
            Button(onClick = {
                scope.launch {
                    currentIndex = (currentIndex + 1) % actualNumberofCardsFetched
                    isVietnameseVisible = false
                }
            }) {
                Text("Next")
            }
        }
    }
}