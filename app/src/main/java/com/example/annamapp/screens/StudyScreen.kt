package com.example.annamapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.core.net.toUri
import androidx.datastore.preferences.core.Preferences
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.annamapp.EMAIL
import com.example.annamapp.TOKEN
import com.example.annamapp.dataStore
import com.example.annamapp.room_sqlite_db.FlashCard
import com.example.annamapp.ui.AudioRequestJSON
import com.example.annamapp.ui.NetworkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.io.encoding.Base64

@Composable
fun StudyScreen(
    onMessageChange: (String) -> Unit = {},
    pickCardLesson: suspend (Int) -> List<FlashCard>,
    networkService: NetworkService
) {
    var numberOfCardsToStudy by rememberSaveable { mutableStateOf(3) }
    var cardList by rememberSaveable { mutableStateOf<List<FlashCard>>(emptyList()) }
    var actualNumberofCardsFetched by rememberSaveable { mutableStateOf(0) }
    var currentIndex by rememberSaveable { mutableStateOf(0) }
    var isVietnameseVisible by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val appContext = context.applicationContext
    val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
    var preferences by remember { mutableStateOf<Preferences?>(null) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var hasLoaded by rememberSaveable { mutableStateOf(false) }
    //remember: prevent new assignment from being loss (reset to initial assignment) after recomposition
    //mutableStateOf: make the variable reactive, that is, automatically detect new assignment and trigger recomposition
    suspend fun loadCards() {
        cardList = pickCardLesson(numberOfCardsToStudy)
        actualNumberofCardsFetched = cardList.size
        currentIndex = 0
        //isVietnameseVisible = false
        if (actualNumberofCardsFetched == 0) {
            onMessageChange("No cards available for study.")
        } else {
            onMessageChange("Tap the text to study.")
        }
    }

    LaunchedEffect(Unit) {
        preferences = preferencesFlow.first()
    }

    if (!hasLoaded) {
        LaunchedEffect(numberOfCardsToStudy) {
            loadCards()
            hasLoaded = true //mutableStateOf makes hasLoaded reactive, if place before loadCards() it will
        }                 //cancel the if condition immediately hence LaunchedEffect() will not run
    }

    if (showDialog) {
        TextInputDialog(
            title = "Enter number of cards to study",
            onConfirm = { input ->
                val num = input.toIntOrNull()
                if (num != null && num > 0) {
                    hasLoaded = false
                    numberOfCardsToStudy = num
                } else {
                    onMessageChange("Invalid input")
                }
            },
            onDismiss = { showDialog = false }
        )
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
        Button(
            onClick = { showDialog = true }, //modifier = Modifier.padding(8.dp)
        ) {
            Text("Loaded $actualNumberofCardsFetched cards. Tap to change")
        }
        Spacer(modifier = Modifier.height(24.dp))

        val currentCard = cardList[currentIndex]
        val displayText = (if (isVietnameseVisible) currentCard.vietnameseCard else currentCard.englishCard) ?: ""
        val title = if (isVietnameseVisible) "Vietnamese" else "English"
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = displayText,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.clickable { isVietnameseVisible = !isVietnameseVisible }
                .padding(12.dp),
            textAlign = TextAlign.Center
        )
        Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(8.dp)
        ) {
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
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val mediaItem = withContext(Dispatchers.IO) {
                                val filename = sha256ofString(displayText)
                                val file = File(context.filesDir, filename)
                                if (!file.exists()) {
                                    val response = networkService.fetchAudio(
                                        cardWithCredential = AudioRequestJSON(
                                            word = displayText,
                                            email = preferences?.get(EMAIL) ?: "",
                                            token = preferences?.get(TOKEN) ?: ""
                                        )
                                    )
                                    if (response.code != 200) {
                                        onMessageChange("Response code is ${response.code}")
                                        return@withContext null
                                    }
                                    val bytes = Base64.decode(response.message)
                                    saveAudioToInternalStorage(file, bytes)
                                }
                                val mediaitem = MediaItem.fromUri(file.absolutePath.toUri())
                                mediaitem
                            } ?: return@launch

                            val player = ExoPlayer.Builder(context).build()
                            player.addListener(object : Player.Listener {
                                override fun onPlaybackStateChanged(playbackState: Int) {
                                    when (playbackState) {
                                        Player.STATE_BUFFERING -> {
                                            // Player is buffering, show a loading indicator if desired
                                            onMessageChange("Buffering...")
                                        }

                                        Player.STATE_READY -> {
                                            // Player is prepared and ready to play
                                            onMessageChange("Ready")
                                        }

                                        Player.STATE_ENDED -> {
                                            // Playback has finished
                                            player.release()
                                            onMessageChange("Finished")
                                        }

                                        Player.STATE_IDLE -> {
                                            // Player is idle, e.g., after release or error
                                        }
                                    }
                                }
                            })
                            player.setMediaItem(mediaItem)
                            player.prepare()
                            player.play()
                        } catch (e: Exception) {
                            onMessageChange("$e")
                        }
                    }
                },
            ) {
                Text("Play sound")
            }
        }
    }
}


fun saveAudioToInternalStorage(file: File, audioData: ByteArray) {
    FileOutputStream(file).use { fos ->
        fos.write(audioData)
    }
}

fun sha256ofString(filename: String): String {
    val bytes = filename.toByteArray()
    val message_digest = java.security.MessageDigest.getInstance("SHA-256")
    val digestBytes = message_digest.digest(bytes)
    val hexString = StringBuilder()
    for (b in digestBytes) {
        val hex = Integer.toHexString(0xff and b.toInt())
        if (hex.length == 1) hexString.append('0')
        hexString.append(hex)
    }
    return hexString.toString()
}

@Composable
fun TextInputDialog(
    title: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var numberOfCardsAsString by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                //fontSize = MaterialTheme.typography.titleSmall.fontSize,
            )
        },
        text = {
            TextField(
                value = numberOfCardsAsString,
                onValueChange = { numberOfCardsAsString = it },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(numberOfCardsAsString)
                    onDismiss()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}