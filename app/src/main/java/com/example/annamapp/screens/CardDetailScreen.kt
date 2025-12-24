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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.example.annamapp.room_sqlite_db.FlashCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
    var englishText by rememberSaveable { mutableStateOf("") }
    var vietnameseText by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var hasLoaded by rememberSaveable { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var showDeleteAudio by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        card = getCardById(cardId)
        if (!hasLoaded) {
            if (card != null) {
                englishText = card?.englishCard ?: ""
                vietnameseText = card?.vietnameseCard ?: ""
                onMessageChange("Edit card details")
                hasLoaded = true
            } else {
                onMessageChange("Card not found")
            }
        }
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp)
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
            val deleteAudioButtonText = if (showDeleteAudio) "Cancel Audio Delete" else "Delete Audio"
            Button(
                onClick = {
                    showDeleteAudio = !showDeleteAudio
                }
            ) {
                Text(deleteAudioButtonText)
            }
            Spacer(modifier = Modifier.width(16.dp))
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
        /*val displayList = rememberSaveable(englishText, vietnameseText) {
            listOf(englishText, vietnameseText)
        }*/
        if (showDeleteAudio) {
            val displayList = rememberSaveable(englishText, vietnameseText) {
                listOf(englishText, vietnameseText)
            }
            //val displayList = rememberSaveable { listOf(englishText, vietnameseText) }
            //val x = displayList[0]
            LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                items(displayList) { text ->
                    WordAudioRow(
                        word = text,
                        onDeleteAudio = { audioWordName ->
                            scope.launch {
                                val info = withContext(Dispatchers.IO) {
                                    val filename = sha256ofString(audioWordName)
                                    val audioFile = File(context.filesDir, filename)
                                    if (audioFile.exists()) {
                                        val deleted = audioFile.delete()
                                        if (deleted) {
                                            return@withContext "Deleted audio file for \"$audioWordName\""
                                        } else {
                                            return@withContext "Failed to delete audio file for \"$audioWordName\""
                                        }
                                    } else {
                                        return@withContext "No audio file found for \"$audioWordName\""
                                    }
                                }
                                onMessageChange(info)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WordAudioRow(
    word: String,
    onDeleteAudio: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = word,
            modifier = Modifier.weight(1f)
        )

        Button(onClick = { onDeleteAudio(word) }) {
            Text("Delete Audio")
        }
    }
}