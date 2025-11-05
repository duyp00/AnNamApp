package com.example.annamapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.annamapp.ui.CardStudyApp
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AnNamDatabase::class.java, "database-name"
        ).build()

        val userDao = db.flashCardDao()
        
        // Launch a coroutine on the lifecycle scope
        lifecycleScope.launch {
            val users: List<FlashCard> = userDao.getAll()
            // You can now use `users` safely here or update your UI through state
        }

        setContent {
            CardStudyApp()
        }
    }
}