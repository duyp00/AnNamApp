package com.example.annamapp.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import java.io.File

@Composable
fun CardDetailScreen(
    enWord: String,
    vnWord: String,
    updateCard: suspend (FlashCard, String, String) -> Unit,
    onMessageChange: (String) -> Unit,
    findByWord: suspend (String, String) -> FlashCard?,
    networkService: NetworkService
    /*deleteCard: suspend (FlashCard) -> Unit,*/ /*onNavigateBack: () -> Unit,*/
    /*getCardById: suspend (Int) -> FlashCard?,*/ /*cardId: Int,*/
) {
    //var card by rememberSaveable { mutableStateOf<FlashCard?>(null) }
    var englishText by rememberSaveable { mutableStateOf(enWord) }
    var vietnameseText by rememberSaveable { mutableStateOf(vnWord) }
    val scope = rememberCoroutineScope()
    var hasLoaded by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val appContext = context.applicationContext
    //move player out of onClick so it is not re-created on every click
    var player by retain { mutableStateOf<ExoPlayer?>(null) }

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
        var enInitial by rememberSaveable { mutableStateOf(enWord) }
        var vnInitial by rememberSaveable { mutableStateOf(vnWord) }
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
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        // Create a new object with same ID but new properties
                        /*val updatedCard = card?.copy(
                            englishCard = englishText,
                            vietnameseCard = vietnameseText
                        ) ?: return@launch*/
                        val updatedCard = findByWord(enInitial, vnInitial)?.copy(
                            englishCard = englishText,
                            vietnameseCard = vietnameseText
                        ) ?: return@launch
                        updateCard(updatedCard, enInitial, vnInitial)
                        //set initials to current after update. use mutableStateOf to be observable
                        enInitial = englishText
                        vnInitial = vietnameseText
                        onMessageChange("Card updated to \"$enInitial\" - \"$vnInitial\"")
                        //onNavigateBack()
                    }
                }
            ) {
                Text("Update card")
            }
        }
        if (openAudioPanel) {
            //live IO file check
            val displayList = rememberSaveable(englishText, vietnameseText) {
                listOf(englishText to "en", vietnameseText to "vi")
            }
            //val displayList = rememberSaveable { listOf(englishText, vietnameseText) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                itemsIndexed(displayList, key = {index, pair -> "${pair.first}-$index"}) { index, (text, language) ->
                    val fileLoadState =
                    produceState<FileLoadforWord?>(initialValue = null, /*key1 = text*/) {
                        value = loadAudioFileFromDiskForText(
                            appContext = appContext,
                            text = text,
                            language = language
                        )
                    }
                    //produceState uses remember internally, so will rerun when config changes
                    val fileLoad = fileLoadState.value
                    if (fileLoad != null) {
                        var existencePositive by rememberSaveable { mutableStateOf(fileLoad.checkExisted) }
                        if (existencePositive) {
                            val audioFile = fileLoad.file
                            var pendingExport by rememberSaveable { mutableStateOf<File?>(null) }
                            pendingExport?.let {
                                ExportLauncher(
                                    file = it,
                                    context = context,
                                    onDone = { pendingExport = null }
                                )
                            }
                            WordAudioDisplay(
                                word = fileLoad.fileName,
                                onDeleteAudio = { //audioWordName ->
                                    scope.launch {
                                        val info = withContext(Dispatchers.IO) {
                                            val deleted = audioFile.delete()
                                            if (deleted) {
                                                existencePositive = false
                                                return@withContext "Deleted audio file for \"$text\""
                                            } else {
                                                return@withContext "Failed to delete audio file for \"$text\""
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
                                },
                                onExportAudio = { //fileName ->
                                    pendingExport = audioFile
                                }
                            )
                        } else {
                            Button(
                                onClick = {
                                    //var downloadText = ""
                                    when(index) {
                                        0 -> {
                                            if (enInitial != englishText) {
                                                onMessageChange("Please save \"$text\" to database before downloading")
                                                return@Button
                                            }
                                            //downloadText = enInitial
                                        }
                                        1 -> {
                                            if (vnInitial != vietnameseText) {
                                                onMessageChange("Please save \"$text\" to database before downloading")
                                                return@Button
                                            }
                                            //downloadText = vnInitial
                                        }
                                    }
                                    scope.launch {
                                        val info = downloadAudioForWord(
                                            appContext = appContext,
                                            networkService = networkService,
                                            text = text, //=downloadText
                                            language = language
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

fun exportFile(
    context: Context,
    source: File,
    destinationUri: Uri
) {
    context.contentResolver.openOutputStream(destinationUri)?.use { output ->
        source.inputStream().use { input ->
            input.copyTo(output)
        }
    }
}

@Composable
fun ExportLauncher(
    file: File,
    context: Context,
    onDone: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let { exportFile(context, file, it) }
        onDone()
    }
    LaunchedEffect(Unit) {
        launcher.launch(file.name)
    }
}

@Composable
fun WordAudioDisplay(
    word: String,
    onDeleteAudio: (String) -> Unit,
    onPlayAudio: (String) -> Unit,
    onExportAudio: (String) -> Unit
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
            Button(
                onClick = { onDeleteAudio(word) },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "Delete Audio",
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
            }
            Spacer(Modifier.width(4.dp))
            Button(
                onClick = { onPlayAudio(word) },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "Play Audio",
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
            }
            Spacer(Modifier.width(4.dp))
            Button(
                onClick = { onExportAudio(word) },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "Export Audio",
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
            }
        }
    }
}