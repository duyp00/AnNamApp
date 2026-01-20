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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.annamapp.room_sqlite_db.FlashCard
import com.example.annamapp.ui.NetworkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CardDetailScreen(
    enWord: String,
    vnWord: String,
    updateCard: suspend (FlashCard, String, String, String, String) -> Unit,
    onNavigateBack: () -> Unit,
    onMessageChange: (String) -> Unit,
    findByWord: suspend (String, String) -> FlashCard?,
    networkService: NetworkService
    /*deleteCard: suspend (FlashCard) -> Unit,*/
    /*getCardById: suspend (Int) -> FlashCard?,*/ /*cardId: Int,*/
) {
    //var card by rememberSaveable { mutableStateOf<FlashCard?>(null) }
    var englishText by rememberSaveable { mutableStateOf(enWord) }
    var vietnameseText by rememberSaveable { mutableStateOf(vnWord) }
    val scope = rememberCoroutineScope()
    var hasLoaded by rememberSaveable { mutableStateOf(false) }
    val appContext = LocalContext.current.applicationContext
    //move player out of onClick so it is not re-created on every click
    var player by retain { mutableStateOf<ExoPlayer?>(null) }
    val enInitial = rememberSaveable { enWord }
    val vnInitial = rememberSaveable { vnWord }

    RetainedEffect(Unit) {
        onRetire {
            if (player != null) {
                player?.release()
                player = null
                onMessageChange("Player released")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLoaded) {
            //card = getCardById(cardId)
            //if (card != null) {
                //englishText = card?.englishCard ?: ""
                //vietnameseText = card?.vietnameseCard ?: ""
                onMessageChange("Edit card details")
                hasLoaded = true
            //} else {
            //    onMessageChange("Card not found")
            //}
        }
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp)
    ) {
        var openAudioPanel by rememberSaveable { mutableStateOf(false) }
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
            /*Button(
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
            ) { Text("Delete") }
            Spacer(modifier = Modifier.width(16.dp))*/
            val deleteAudioButtonText = if (openAudioPanel) "Close Audio Panel" else "Manage Audio"
            Button(
                onClick = {
                    openAudioPanel = !openAudioPanel
                }
            ) {
                Text(deleteAudioButtonText)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        // Create a new object with same ID but new properties
                        /*val updatedCard = card?.copy(
                            englishCard = englishText,
                            vietnameseCard = vietnameseText
                        ) ?: return@launch*/
                        val updatedCard = findByWord(enWord, vnWord)?.copy(
                            englishCard = englishText,
                            vietnameseCard = vietnameseText
                        ) ?: return@launch
                        updateCard(updatedCard, enInitial, vnInitial, englishText, vietnameseText)
                        //set initials to current after update. use mutableStateOf to be observable
                        //enInitial = englishText
                        //vnInitial = vietnameseText
                        //onMessageChange("Card updated") //disabled because navigating back
                        onNavigateBack()                  //immediately overrides message
                    }
                }
            ) {
                Text("Update card")
            }
        }
        if (openAudioPanel) {
            //live IO file check disabled due to duplicate string case, which crashes compose
            //val displayList = rememberSaveable(englishText, vietnameseText) {
            //    listOf(englishText, vietnameseText)
            //}
            val displayList = rememberSaveable { listOf(englishText, vietnameseText) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                items(displayList, /*key = { it }*/) { text ->
                    val fileLoadState =
                    produceState<FileLoadforWord?>(initialValue = null, /*key1 = text*/)
                    { value = loadAudioFileFromDiskForText(appContext, text) }
                    //produceState uses remember internally, so will rerun when config changes
                    val fileLoad = fileLoadState.value
                    if (fileLoad != null) {
                        val audioFile = fileLoad.file
                        var existencePositive by rememberSaveable { mutableStateOf(fileLoad.checkExisted) }
                        if (existencePositive) {
                            WordAudioDisplay(
                                word = fileLoad.fileName,
                                onDeleteAudio = { audioWordName ->
                                    scope.launch {
                                        val info = withContext(Dispatchers.IO) {
                                            val deleted = audioFile.delete()
                                            if (deleted) {
                                                existencePositive = false
                                                return@withContext "Deleted audio file for \"$audioWordName\""
                                            } else {
                                                return@withContext "Failed to delete audio file for \"$audioWordName\""
                                            }
                                        }
                                        onMessageChange(info)
                                    }
                                },
                                onPlayAudio = { //wordToPlay ->
                                    scope.launch {
                                        if (player == null) { player = instantiatePlayer(
                                            appContext = appContext,
                                            onMessageChange = onMessageChange
                                        )}
                                        val mediaItem = MediaItem.fromUri(audioFile.absolutePath.toUri())
                                        player?.setMediaItem(mediaItem)
                                        player?.prepare()
                                        player?.play()
                                    }
                                }
                            )
                        } else {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val info = downloadAudioForWord(
                                            appContext = appContext,
                                            networkService = networkService,
                                            text = text
                                        )
                                        if (info.status == "SUCCESS") {
                                            existencePositive = true
                                        } else {
                                            onMessageChange((info as AudioLoadResult.Error).message)
                                        }
                                    }
                                }
                            ) { Text("Download audio for \"$text\"") }
                        }
                    } else { Text("Checking...") }
                }
            }
        }
    }
}

@Composable
fun WordAudioDisplay(
    word: String,
    onDeleteAudio: (String) -> Unit,
    onPlayAudio: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(3.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = word)
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { onDeleteAudio(word) }) {
                Text("Delete Audio")
            }
            Spacer(Modifier.width(4.dp))
            Button(onClick = { onPlayAudio(word) }) {
                Text("Play Audio")
            }
        }
    }
}