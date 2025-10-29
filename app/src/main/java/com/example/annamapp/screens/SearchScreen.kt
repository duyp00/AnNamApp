package com.example.annamapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen(
    onMessageChange: (String) -> Unit = {}
) {
    //LaunchedEffect(Unit) {
    onMessageChange("search by english or vietnamese word")
    //}
    var query by remember { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search query") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            // placeholder search
        }) {
            Text("Search")
        }

        Spacer(Modifier.height(24.dp))
        Text("(Search results placeholder. Press Back in top bar to return.)")
    }
}

@Preview(/*showBackground = true*/)
@Composable
fun PreviewSearchScreen() {
    // It's a good practice to wrap previews inside theme
    // M3 theme {
    SearchScreen()
    // }
}