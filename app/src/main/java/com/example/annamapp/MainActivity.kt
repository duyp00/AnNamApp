package com.example.annamapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.annamapp.room_sqlite_db.AnNamDatabase
import com.example.annamapp.ui.CardStudyApp
import com.example.annamapp.ui.NetworkService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
        }*/

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://placeholder.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val networkService = retrofit.create(NetworkService::class.java)

        setContent {
            val navController = rememberNavController()
            CardStudyApp(flashCardDao = flashCardDao, navController = navController, networkService = networkService)
        }
    }
}