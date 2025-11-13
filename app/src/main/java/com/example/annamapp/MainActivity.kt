package com.example.annamapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.annamapp.ui.CardStudyApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, AnNamDatabase::class.java, "myapp_database").build()
        val userDao = db.flashCardDao()

        /*
        lifecycleScope.launch {
            userDao.insertAll(FlashCard(
                uid = 0,
                englishCard = "test13",
                vietnameseCard = "test14"
            ))
            val users: List<FlashCard> = userDao.getAll()
            Log.d("AnNam", users.toString())
        }
         */

        setContent {
            CardStudyApp(userDao)
        }
    }
}