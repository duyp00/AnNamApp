package com.example.annamapp.screens

import android.content.Context
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
import com.example.annamapp.networking.NetworkService
import com.example.annamapp.room_sqlite_db.FlashCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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

    LaunchedEffect(hasLoaded) {
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
                    numberOfCardsToStudy = num
                    hasLoaded = false
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
                        try {
                            if (isPlayerPlaying) {
                                player?.pause()
                            } else {
                                if (playNew) {
                                    val fileLoad = loadAudioFileFromDiskForText(
                                        appContext = appContext,
                                        text = displayText,
                                        language = if (isVietnameseVisible) "vi" else "en"
                                    )
                                    if (!fileLoad.checkExisted) {
                                        val result = downloadAudioForWord(
                                            appContext = appContext,
                                            text = displayText,
                                            language = if (isVietnameseVisible) "vi" else "en",
                                            networkService = networkService
                                        )
                                        if (result.status == "ERROR") {
                                            onMessageChange((result as AudioLoadResult.Error).message)
                                            return@launch
                                        }
                                    }
                                    val file = fileLoad.file
                                    val mediaItem = MediaItem.fromUri(file.absolutePath.toUri())
                                    if (player == null) { player = instantiatePlayer(
                                        appContext = appContext,
                                        onMessageChange = onMessageChange,
                                        onPlayerStateChange = { state -> playerState = state },
                                        onPlayingChange = { playing -> isPlayerPlaying = playing }
                                    )}
                                    player?.setMediaItem(mediaItem)
                                    player?.prepare()
                                    playNew = false
                                } else if (playerState == 4) {
                                    //no need to set media again. go to start of audio
                                    player?.seekTo(0)
                                }
                                player?.play()
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

fun instantiatePlayer(
    appContext: Context,
    onMessageChange: (String) -> Unit = {},
    onPlayerStateChange: (Int) -> Unit = {},
    onPlayingChange: (Boolean) -> Unit = {}
): ExoPlayer {
    return ExoPlayer.Builder(appContext).build().apply {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                onPlayerStateChange(playbackState)
                when (playbackState) {
                    Player.STATE_BUFFERING -> onMessageChange("Buffering...") //num = 2
                    Player.STATE_READY -> onMessageChange("Ready") //num = 3. prepared and ready to play
                    Player.STATE_ENDED -> onMessageChange("Finished") //num = 4
                    Player.STATE_IDLE -> Unit //num = 1. idle, e.g., after release or error
                }
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onPlayingChange(isPlaying)
            }
        })
    }
}

suspend fun loadAudioFileFromDiskForText(
    appContext : Context,
    text: String,
    language: String
): FileLoadforWord {
    val filename = withContext(Dispatchers.Default) {
        sha256ofString("$language|$text")
    }
    return withContext(Dispatchers.IO) {
        //directory or file path, treated as a file in Linux
        val dir = appContext.filesDir
        val audioFile = File(dir, filename)
        FileLoadforWord(
            file = audioFile,
            fileName = filename,
            checkExisted = audioFile.exists()
        )
    }
}

suspend fun downloadAudioForWord(
    appContext: Context,
    text: String,
    language: String,
    networkService: NetworkService
): AudioLoadResult {
    try {
        val filename = withContext(Dispatchers.Default) {
            sha256ofString("$language|$text")
        }
        return withContext(Dispatchers.IO) {
            val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
            val preferences: Preferences = preferencesFlow.first()
            val hasEmail = !preferences[EMAIL].isNullOrBlank()
            val hasToken = !preferences[TOKEN].isNullOrBlank()
            if (!hasEmail || !hasToken) {
                return@withContext AudioLoadResult.Error(
                    "Please log in to access audio downloads"
                )
            }
            if (text.isBlank()) {
                return@withContext AudioLoadResult.Error("Text is required for audio")
            }
            /*if (language != "en" && language != "vi") {
                return@withContext AudioLoadResult.Error("Unsupported language: $language")
            }*/
            val response = networkService.fetchAudio(
                text = text,
                language = language
            )
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                val errorSuffix = if (errorBody.isNullOrBlank()) "" else ": $errorBody"
                return@withContext AudioLoadResult.Error(
                    "Response code is ${response.code()}$errorSuffix"
                )
            }
            val bytes = response.body()?.bytes() ?: run {
                //.bytes() already consumes and closes the stream according to okhttp docs
                //return@run ByteArray(0)
                return@withContext AudioLoadResult.Error("Empty audio response")
            }
            val file = File(appContext.filesDir, filename)
            saveAudioToInternalStorage(file, bytes)
            AudioLoadResult.Success("Good to go")//implicit return
        }
    }
    catch (e: Exception) {
        return AudioLoadResult.Error("Download failed: $e")
    }
}

sealed class AudioLoadResult { //overkill but useful if more data to return
    data class Success(val message: String): AudioLoadResult()
    data class Error(val message: String): AudioLoadResult()
    val status: String
        get() = when (this) {
            is Success -> "SUCCESS"
            //is Error -> "ERROR"
            else -> "ERROR"
        }
}

data class FileLoadforWord(
    val file: File,
    val fileName: String,
    val checkExisted: Boolean = false
)

fun saveAudioToInternalStorage(file: File, audioData: ByteArray) {
    FileOutputStream(file).use { fos ->
        fos.write(audioData)
        //fos.flush()
        //fos.close() //not needed because .use() close automatically
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