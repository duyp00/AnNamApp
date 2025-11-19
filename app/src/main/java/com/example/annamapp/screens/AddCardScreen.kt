package com.example.annamapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.annamapp.room_sqlite_db.FlashCard
import com.example.annamapp.R
import kotlinx.coroutines.launch

@Composable
fun AddCardScreen(
    insertFlashCard: suspend (FlashCard) -> Unit,
    onMessageChange: (String) -> Unit = {}
) {
    LaunchedEffect(Unit) { //according to chatgpt i should use this but for now everything is still fine
        onMessageChange("Add your cards now")
    }
    //var clickOnAdd by remember { mutableStateOf(false) }
    var enWord by remember { mutableStateOf("") }
    var vnWord by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    //var enWord by rememberSaveable { mutableStateOf("") }
    //var vnWord by rememberSaveable { mutableStateOf("") }
    Column {
        TextField(
            value = enWord,
            onValueChange = { enWord = it },
            modifier = Modifier.semantics { contentDescription = "English String" },
            label = { Text(stringResource(R.string.english_label)) }
        )
        TextField(
            value = vnWord,
            onValueChange = { vnWord = it },
            label = { Text(stringResource(R.string.vietnamese_label)) }
        )

        /*
        if (clickOnAdd) {
            Text("The card is [$enWord, $vnWord] ...")
        }
        else { {} }
        */

        Row {
            Button(onClick = {
                //clickOnAdd = true
                scope.launch {
                    insertFlashCard(
                        FlashCard(
                            uid = 0, //best value to choose is 0 since it's auto-generated
                            englishCard = enWord,
                            vietnameseCard = vnWord
                        )
                    )
                    onMessageChange("Added card: [$enWord, $vnWord]")
                    enWord = ""
                    vnWord = ""
                }
            })
            { Text("Add") }
        }
    }
}