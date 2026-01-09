package com.example.annamapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.sqlite.SQLiteException
import com.example.annamapp.R
import com.example.annamapp.room_sqlite_db.FlashCard
import kotlinx.coroutines.launch

@Composable
fun AddCardScreen(
    insertFlashCard: suspend (FlashCard) -> Unit,
    findByWord: suspend (String, String) -> FlashCard?,
    onMessageChange: (String) -> Unit = {}
) {
    LaunchedEffect(Unit) { //according to chatgpt i should use this
        onMessageChange("Add your cards now")
    }
    //var clickOnAdd by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var enWord by rememberSaveable { mutableStateOf("") }
    var vnWord by rememberSaveable { mutableStateOf("") }
    Column {
        TextField(
            value = enWord,
            onValueChange = { enWord = it },
            modifier = Modifier.semantics { contentDescription = "enTextField" },
            label = { Text(stringResource(R.string.english_label)) }
        )
        TextField(
            value = vnWord,
            onValueChange = { vnWord = it },
            modifier = Modifier.semantics { contentDescription = "vnTextField" },
            label = { Text(stringResource(R.string.vietnamese_label)) }
        )

        /*
        if (clickOnAdd) {
            Text("The card is [$enWord, $vnWord] ...")
        }
        else { {} }*/

        Button(onClick = {
            //clickOnAdd = true
            scope.launch {
                try {
                    if (findByWord(enWord, vnWord) != null) {
                        throw SQLiteException("Card already exists in database.")
                    }
                    insertFlashCard(
                        FlashCard(
                            uid = 0, //best value to choose is 0 since it's auto-generated
                            englishCard = enWord,
                            vietnameseCard = vnWord
                        )
                    )
                    onMessageChange("Added card: [$enWord, $vnWord]")
                } catch (ex: Exception) {
                    onMessageChange("$ex") //equivalent to ex.toString() (no double quotes). if more friendly: ex.localizedMessage
                } finally {
                    enWord = ""
                    vnWord = ""
                }
            }
        }, modifier = Modifier.semantics { contentDescription = "add_card_button" })
        { Text("Add") }
    }
}