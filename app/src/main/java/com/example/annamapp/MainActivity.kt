package com.example.annamapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import com.example.annamapp.room_sqlite_db.AnNamDatabase
import com.example.annamapp.ui.CardStudyApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, AnNamDatabase::class.java, "annamapp_flashcard_db").build()
        val flashCardDao = db.flashCardDao()

        /*
        lifecycleScope.launch {
            flashCardDao.insertAll(FlashCard(
                uid = 0,
                englishCard = "test13",
                vietnameseCard = "test14"
            ))
            val users: List<FlashCard> = flashCardDao.getAll()
            Log.d("AnNam", users.toString())
        }
         */

        setContent {
            CardStudyApp(flashCardDao)
        }
    }
}