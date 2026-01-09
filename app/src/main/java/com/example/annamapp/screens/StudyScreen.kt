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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
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
    var cardList by rememberSaveable { mutableStateOf<List<FlashCard>>(listOf()) }
    var actualNumberofCardsFetched by rememberSaveable { mutableStateOf(0) }
    var currentIndex by rememberSaveable { mutableStateOf(0) }
    var isVietnameseVisible by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val appContext = context.applicationContext
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var hasLoaded by rememberSaveable { mutableStateOf(false) }
    var isPlayerPlaying by rememberSaveable { mutableStateOf(false) }
    var playerState by rememberSaveable { mutableStateOf(0) }
    //move player out of onClick so it is not re-created on every click
    var player by retain { mutableStateOf<ExoPlayer?>(null) }
    fun instantiatePlayer(): ExoPlayer {
        return ExoPlayer.Builder(appContext).build().apply {//for retain(), use app context instead of
            addListener(object: Player.Listener { //activity context to avoid memory leaks
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            onMessageChange("Buffering...")
                            playerState = 2
                        }
                        Player.STATE_READY -> { // Player is prepared and ready to play
                            onMessageChange("Ready")
                            playerState = 3
                        }
                        Player.STATE_ENDED -> {
                            onMessageChange("Finished")
                            playerState = 4
                        }
                        Player.STATE_IDLE -> Unit // Player is idle, e.g., after release or error
                    }
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    isPlayerPlaying = isPlaying
                }
            })
        }
    }
    RetainedEffect(Unit) {
        onRetire {
            if (player != null) {
                player!!.release()
                player = null
                onMessageChange("Player released")
            }
        }
    }
    //retain is new API (now stable Dec 2025). kinda between remember and rememberSaveable (survives config changes,
    //not process death). refer https://developer.android.com/develop/ui/compose/state-lifespans#retain
    //remember: prevent new assignment from being loss (reset to initial assignment) after recomposition
    //mutableStateOf: make the variable reactive, that is, automatically detect new assignment and trigger recomposition
    suspend fun loadCards() {
        cardList = pickCardLesson(numberOfCardsToStudy)
        actualNumberofCardsFetched = cardList.size
        currentIndex = 0 //changing numberOfCardsToStudy from higher to lower may cause outOfBounds
        //isVietnameseVisible = false
        if (actualNumberofCardsFetched == 0) {
            onMessageChange("No cards available for study.")
        } else {
            onMessageChange("Tap the text to study.")
        }
    }

    LaunchedEffect(numberOfCardsToStudy) {
        if (!hasLoaded) {
            loadCards()
            hasLoaded = true
        }
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
        var playNew by rememberSaveable { mutableStateOf(true) }

        Text(
            text = if (isVietnameseVisible) "Vietnamese" else "English",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = displayText,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.clickable {
                isVietnameseVisible = !isVietnameseVisible
                //to play new audio if switched during playing, click pause then play
                playNew = true
                //player.stop()
            }.padding(12.dp),
            textAlign = TextAlign.Center
        )
        Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            if (isVietnameseVisible) {
                Button(onClick = {
                    currentIndex = (currentIndex + 1) % actualNumberofCardsFetched
                    isVietnameseVisible = false
                    //same reason as above
                    playNew = true
                }) {
                    Text("Next")
                }
            }
            Button(
                onClick = {
                    scope.launch {
                        try { //non-null asserted is used since player is instantiated before use
                            if (isPlayerPlaying) {
                                player!!.pause()
                            } else {
                                if (playNew) {
                                    val filename = withContext(Dispatchers.Default) {
                                        sha256ofString(displayText)
                                    }
                                    val result = withContext(Dispatchers.IO) {
                                        //directory or file path, treated as a file in Linux
                                        val dir = appContext.filesDir
                                        val file = File(dir, filename)
                                        if (!file.exists()) {
                                            val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
                                            val preferences: Preferences = preferencesFlow.first()
                                            val response = networkService.fetchAudio(
                                                cardWithCredential = AudioRequestJSON(
                                                    word = displayText,
                                                    email = preferences[EMAIL] ?: return@withContext
                                                    AudioLoadResult.Error("Email not found"),
                                                    token = preferences[TOKEN] ?: return@withContext
                                                    AudioLoadResult.Error("Token not found")
                                                )
                                            )
                                            if (response.code != 200) {
                                                //should not call onMessageChange from non-UI thread
                                                return@withContext AudioLoadResult.Error(
                                                    "Response code is ${response.code}"
                                                )
                                            }
                                            val bytes = Base64.decode(response.message)
                                            saveAudioToInternalStorage(file, bytes)
                                        }
                                        val mediaitem = MediaItem.fromUri(file.absolutePath.toUri())
                                        AudioLoadResult.Success(mediaItem = mediaitem)//implicit return
                                    }
                                    if (result.status == "ERROR") {
                                        onMessageChange((result as AudioLoadResult.Error).message)
                                        return@launch
                                    }
                                    val mediaItem = (result as AudioLoadResult.Success).mediaItem
                                    if (player == null) { player = instantiatePlayer() }
                                    player!!.setMediaItem(mediaItem)
                                    player!!.prepare()
                                    playNew = false
                                } else if (playerState == 4) {
                                    //no need to set media again. go to start of audio
                                    player!!.seekTo(0)
                                }
                                player!!.play()
                            }
                        } catch (e: Exception) {
                            onMessageChange("$e")
                        }
                    }
                },
            ){ Text(
                if (isPlayerPlaying) {"Pause Audio"}
                else if (playNew) {"Play Audio"}
                else {"Resume Audio"}
            )}
        }
    }
}

sealed class AudioLoadResult {
    data class Success(val mediaItem: MediaItem): AudioLoadResult()
    data class Error(val message: String): AudioLoadResult()
    val status: String
        get() = when (this) {
            is Success -> "SUCCESS"
            //is Error -> "ERROR"
            else -> "ERROR"
        }
}

fun saveAudioToInternalStorage(file: File, audioData: ByteArray) {
    FileOutputStream(file).use { fos ->
        fos.write(audioData)
    }
}

fun sha256ofString(string: String): String {
    val bytes = string.toByteArray()
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