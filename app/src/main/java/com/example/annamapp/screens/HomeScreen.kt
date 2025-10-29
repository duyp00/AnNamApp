package com.example.annamapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.annamapp.navigation.Routes

@Composable
fun HomeScreen(
    onNavigateToStudy: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onMessageChange: (String) -> Unit = {}
) {
    //LaunchedEffect(Unit) {
        onMessageChange("this is home screen")
    //}
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Menu",
            style = MaterialTheme.typography.headlineMedium, // M3 typography scale
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = onNavigateToStudy,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Study Cards")
        }

        Button(
            onClick = onNavigateToAdd,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription = "openAddCardScreen"
                }
        ) {
            Text("Add a Card")
        }

        Button(
            onClick = onNavigateToSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Search Cards")
        }
    }
}

@Preview(/*showBackground = true*/)
@Composable
fun PreviewHomeScreen(
    navCtrller: NavHostController = rememberNavController()
) {
    // It's a good practice to wrap previews inside theme
    // M3 theme {
    HomeScreen(
        onNavigateToStudy = { navCtrller.navigate(Routes.STUDY) },
        onNavigateToAdd = { navCtrller.navigate(Routes.ADD) },
        onNavigateToSearch = { navCtrller.navigate(Routes.SEARCH) }
    )
    // }
}