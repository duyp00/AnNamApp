package com.example.annamapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.annamapp.navigation.Routes
import com.example.annamapp.room_sqlite_db.FlashCard
import kotlinx.coroutines.launch

@Composable
fun SearchResultScreen(
    filters: Routes.SearchResults,
    performSearch: suspend (Routes.SearchResults) -> List<FlashCard>,
    deleteCards: suspend (List<FlashCard>) -> Unit,
    onNavigateToCard: (Int) -> Unit,
    onMessageChange: (String) -> Unit = {}
) {
    var results by rememberSaveable { mutableStateOf<List<FlashCard>>(listOf()) }
    var selectedCardIds by rememberSaveable { mutableStateOf<Set<Int>>(setOf()) }
    val scope = rememberCoroutineScope()
    var hasLoaded by rememberSaveable { mutableStateOf(false) }

    suspend fun refresh() {
        results = performSearch(filters)
        //selectedCardIds = setOf() //clear selection on refresh
        if (results.isEmpty()) {
            onMessageChange("No cards found")
        } else {
            onMessageChange("Tap a card or use Delete to remove selected")
        }
    }

    LaunchedEffect(Unit) {
        //key1 = filters would also work, but filters only change when navigating to this screen
        if (!hasLoaded) { //if want to see changes after updating cards, disable this guard,
            //but huge performance drop if DB is large just to navigate back
            refresh()
            hasLoaded = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Results", style = MaterialTheme.typography.titleMedium)
            Button(
                enabled = selectedCardIds.isNotEmpty(),
                onClick = {
                    scope.launch {
                        val toDelete = results.filter { selectedCardIds.contains(it.uid) }
                        /* //this is never reached because button is disabled when no selection
                        if (toDelete.isEmpty()) {
                            onMessageChange("Select card(s) to delete")
                            return@launch //early abort since there is nothing to delete (read kotlin return@ for more)
                        }*/
                        deleteCards(toDelete)
                        onMessageChange("Deleted ${toDelete.size} card(s)")
                        refresh()
                    }
                }
            ) {
                Text("Delete")
            }
        }

        if (results.isEmpty()) {
            Text(text = "No cards to show", modifier = Modifier.padding(top = 24.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                items(results, /*key = { it.uid }*/) { card ->
                    ResultRow(
                        card = card,
                        isSelected = selectedCardIds.contains(card.uid),
                        onSelectionChange = { checked ->
                            selectedCardIds = if (checked) {
                                selectedCardIds + card.uid//'+' here is adding element to set, creating new (immutable) set
                            } else {                              //then reassign new set object to trigger recomposition
                                selectedCardIds - card.uid//'-' same as above but removing element
                            }
                        },
                        onViewClick = { onNavigateToCard(card.uid) }
                    )
                }
            }
        }
    }
}

@Composable
fun ResultRow(
    card: FlashCard,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    onViewClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(3.dp),
        //verticalAlignment = Alignment.CenterVertically,
        //horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Checkbox(checked = isSelected, onCheckedChange = onSelectionChange)

        Column(
            modifier = Modifier.weight(1f)//.padding(horizontal = 12.dp)
        ) {
            Text(text = card.englishCard.orEmpty(), /*style = MaterialTheme.typography.bodyMedium*/)
            Text(text = card.vietnameseCard.orEmpty(), /*style = MaterialTheme.typography.bodyMedium*/)
        }

        Button(onClick = onViewClick) {
            Text("View")
        }
    }
}
