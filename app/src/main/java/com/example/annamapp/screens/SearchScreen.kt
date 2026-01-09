package com.example.annamapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.annamapp.navigation.Routes

@Composable
fun SearchScreen(
    onSearch: (Routes.SearchResults) -> Unit,
    onShowAllCards: () -> Unit,
    onMessageChange: (String) -> Unit = {}
) {
    var englishQuery by rememberSaveable { mutableStateOf("") }
    var englishEnabled by rememberSaveable { mutableStateOf(true) }
    var englishWholeWord by rememberSaveable { mutableStateOf(false) }

    var vietnameseQuery by rememberSaveable { mutableStateOf("") }
    var vietnameseEnabled by rememberSaveable { mutableStateOf(true) }
    var vietnameseWholeWord by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onMessageChange("Enable a field, enter text, then search")
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SearchFieldSection(
            label = "English",
            value = englishQuery,
            onValueChange = { englishQuery = it },
            enabled = englishEnabled,
            onEnabledChange = { englishEnabled = it },
            wholeWord = englishWholeWord,
            onWholeWordChange = { englishWholeWord = it }
        )

        SearchFieldSection(
            label = "Vietnamese",
            value = vietnameseQuery,
            onValueChange = { vietnameseQuery = it },
            enabled = vietnameseEnabled,
            onEnabledChange = { vietnameseEnabled = it },
            wholeWord = vietnameseWholeWord,
            onWholeWordChange = { vietnameseWholeWord = it }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val canSearch = (englishEnabled && englishQuery.isNotBlank())
                || (vietnameseEnabled && vietnameseQuery.isNotBlank())
            Button(
                modifier = Modifier.weight(1f),
                enabled = canSearch,
                onClick = {
                    /* //this is never reached due to button being disabled already
                    if (!canSearch) {
                        onMessageChange("Select at least one field and enter text")
                        return@Button //early abort since there is nothing to search (read kotlin return@ for more)
                    }*/

                    val filters = Routes.SearchResults(
                        englishQuery = englishQuery.trim(),
                        englishEnabled = englishEnabled && englishQuery.isNotBlank(),
                        englishWholeWord = englishWholeWord /*&& englishEnabled*/,
                        vietnameseQuery = vietnameseQuery.trim(),
                        vietnameseEnabled = vietnameseEnabled && vietnameseQuery.isNotBlank(),
                        vietnameseWholeWord = vietnameseWholeWord /*&& vietnameseEnabled*/
                    )
                    onSearch(filters)
                }
            ) {
                Text("Search")
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = onShowAllCards
            ) {
                Text("Show all cards")
            }
        }
    }
}

@Composable
fun SearchFieldSection(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    wholeWord: Boolean,
    onWholeWordChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            label = { Text("$label word") },
            singleLine = true,
            /*
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            )*/
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CheckboxRow(
                text = "Search $label",
                checked = enabled,
                onCheckedChange = onEnabledChange
            )

            CheckboxRow(
                text = "Whole word",
                checked = wholeWord,
                onCheckedChange = onWholeWordChange,
                //enabled = enabled
            )
        }
    }
}

@Composable
fun CheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    //enabled: Boolean = true
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = { onCheckedChange(it) }, /*enabled = enabled*/)
        Text(text)
    }
}