package com.example.annamapp.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.annamapp.room_sqlite_db.FlashCard
import com.example.annamapp.room_sqlite_db.FlashCardDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

@Composable
fun BackupScreen(
    flashCardDao: FlashCardDao,
    onMessageChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var databaseRows by rememberSaveable { mutableStateOf<List<FlashCard>>(listOf()) }
    val selectedExportIds = rememberSaveable { mutableStateListOf<Long>() }
    var parsedImportRows by rememberSaveable { mutableStateOf<List<FlashCard>>(listOf()) }
    val selectedImportIndexes = rememberSaveable { mutableStateListOf<Int>() }
    var hasLoaded by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            hasLoaded = false
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLoaded) {
            databaseRows = flashCardDao.getAll()
            selectedExportIds.clear()
            selectedExportIds.addAll(databaseRows.map { it.uid })
            hasLoaded = true
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val rowsToExport = databaseRows.filter { selectedExportIds.contains(it.uid) }
        val content = backupJson.encodeToString(rowsToExport)
        scope.launch {
            val writeSuccess = withContext(Dispatchers.IO) {
                //runCatching() is used to handle potential exceptions when writing to the URI, such as IO errors or permission issues. It returns a Result<Boolean> indicating success or failure.
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(content.toByteArray())
                    }
                }.isSuccess
            }
            onMessageChange(
                if (writeSuccess) { "Exported ${rowsToExport.size} row(s)." }
                else { "Export failed." }
            )
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val fileContent = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() }
                }.getOrNull()
            }
            val rows = fileContent?.let { parseAndValidateBackupRows(it) }
            selectedImportIndexes.clear()
            if (rows == null) {
                parsedImportRows = listOf()
                onMessageChange("Invalid backup file or schema.")
                return@launch
            }
            parsedImportRows = rows
            selectedImportIndexes.addAll(rows.indices)
            onMessageChange("Loaded ${rows.size} row(s) from backup.")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        //verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { importFileLauncher.launch("application/json") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Import")
        }

        if (parsedImportRows.isNotEmpty()) {
            Text("Select rows to import (${selectedImportIndexes.size}/${parsedImportRows.size})")
            parsedImportRows.forEachIndexed { index, row ->
                CardsSelectPreview(
                    checked = selectedImportIndexes.contains(index),
                    card = row,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            if (!selectedImportIndexes.contains(index)) selectedImportIndexes.add(index)
                        } else {
                            selectedImportIndexes.remove(index)
                        }
                    }
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        val selectedRows = selectedImportIndexes.map { parsedImportRows[it] }
                        //val existingRows = flashCardDao.getAll()
                        //val rowsToInsert = getRowsToImport(existingRows, selectedRows)
                        //-1L is returned by Room when an insert operation fails (e.g., due to a conflict or constraint violation).
                        val rowsToInsert = selectedRows
                        var insertCount = 0
                        if (rowsToInsert.isNotEmpty()) {
                            insertCount = flashCardDao.insertAll(*rowsToInsert.toTypedArray()).count { it != -1L }
                            databaseRows = flashCardDao.getAll()
                            selectedExportIds.clear()
                            selectedExportIds.addAll(databaseRows.map { it.uid })
                        }
                        onMessageChange("Imported $insertCount new row(s).")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import selected rows")
            }
        }

        Button(
            onClick = { createDocumentLauncher.launch("AnNam_flashcard_backup.json") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export")
        }

        Text("Select rows to export (${selectedExportIds.size}/${databaseRows.size})")
        databaseRows.forEach { card ->
            CardsSelectPreview(
                checked = selectedExportIds.contains(card.uid),
                card = card,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        if (!selectedExportIds.contains(card.uid)) selectedExportIds.add(card.uid)
                    } else {
                        selectedExportIds.remove(card.uid)
                    }
                }
            )
        }
    }
}

@Composable
fun CardsSelectPreview(
    checked: Boolean,
    card: FlashCard,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text("${card.englishCard.orEmpty()}\n${card.vietnameseCard.orEmpty()}")
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp))
}

val backupJson = Json { ignoreUnknownKeys = true }

fun parseAndValidateBackupRows(content: String): List<FlashCard>? {
    val root: JsonElement = runCatching { backupJson.parseToJsonElement(content) }.getOrNull() ?: return null
    val array: JsonArray = runCatching { root.jsonArray }.getOrNull() ?: return null
    if (!hasValidBackupSchema(array)) return null
    return runCatching {
        array.map {
            val row = it.jsonObject
            FlashCard(
                uid = row["uid"]?.let { value ->
                    if (value is JsonPrimitive) { value.contentOrNull?.toLongOrNull() }
                    else null
                } ?: 0L,
                englishCard = row["englishCard"]?.let { value ->
                    if (value is JsonPrimitive) { value.contentOrNull }
                    else null
                },
                vietnameseCard = row["vietnameseCard"]?.let { value ->
                    if (value is JsonPrimitive) { value.contentOrNull }
                    else null
                }
            )
        }
    }.getOrNull()
}

fun hasValidBackupSchema(array: JsonArray): Boolean {
    return array.all { element ->
        val row: JsonObject = runCatching { element.jsonObject }.getOrNull() ?: return false
        val hasEnglish = row.containsKey("englishCard")
        val hasVietnamese = row.containsKey("vietnameseCard")
        val englishValid = row["englishCard"] is JsonPrimitive || row["englishCard"] is JsonNull
        val vietnameseValid = row["vietnameseCard"] is JsonPrimitive || row["vietnameseCard"] is JsonNull
        val uidValid = row["uid"] == null || row["uid"] is JsonPrimitive || row["uid"] is JsonNull
        hasEnglish && hasVietnamese && englishValid && vietnameseValid && uidValid
    }
}

/*@Serializable
//in the JSON file, the field names will be "english_card" and "vietnamese_card" instead of "englishCard" and "vietnameseCard". this maintains a specific naming convention in the JSON file that differs from the Kotlin property names.
data class BackupFlashCardRow(
    val uid: Long? = null,
    @SerialName("english_card") val englishCard: String? = null,
    @SerialName("vietnamese_card") val vietnameseCard: String? = null
)*/

/*fun exportRowsToJson(cards: List<FlashCard>): String {
    val rows = cards.map {
        BackupFlashCardRow(
            uid = it.uid,
            englishCard = it.englishCard,
            vietnameseCard = it.vietnameseCard
        )
    }
    return backupJson.encodeToString(rows)
}*/

/*fun getRowsToImport(existingCards: List<FlashCard>, incomingRows: List<FlashCard>): List<FlashCard> {
    val existingKeySet = existingCards.map { it.englishCard to it.vietnameseCard }.toMutableSet()
    val rowsToInsert = mutableListOf<FlashCard>()
    for (row in incomingRows) {
        val key = row.englishCard to row.vietnameseCard
        if (existingKeySet.add(key)) {
            rowsToInsert.add(
                FlashCard(
                    englishCard = key.first,
                    vietnameseCard = key.second
                )
            )
        }
    }
    return rowsToInsert
}*/