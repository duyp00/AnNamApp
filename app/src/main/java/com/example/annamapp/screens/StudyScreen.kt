package com.example.annamapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun StudyScreen(
    onMessageChange: (String) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        onMessageChange("this is study screen")
    }
    // Simple placeholder: may replace with real study-card UI
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Study Cards Screen\n\n(Press Back in top bar to return)",
            textAlign = TextAlign.Center
        )
    }
}