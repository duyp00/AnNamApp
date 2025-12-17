package com.example.annamapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.annamapp.room_sqlite_db.AnNamDatabase
import com.example.annamapp.ui.CardStudyApp
import com.example.annamapp.ui.NetworkService
import com.example.annamapp.ui.theme.AnNamAppTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//The Preferences DataStore implementation uses the DataStore and Preferences classes to persist key-value pairs to disk.
//Use the property delegate created by preferencesDataStore to create an instance of DataStore<Preferences>.
//Call it once at the top level of your Kotlin file. Access DataStore through this property
//throughout the rest of your application. This makes it easier to keep your DataStore as a singleton.
val Context.dataStore by preferencesDataStore(
    name = "user_credentials"
)

//Because Preferences DataStore doesn't use a predefined schema,
//you must use the corresponding key type function to define a key for each value that you need to store
//in the DataStore<Preferences> instance.
//For example, to define a key for an int value, use intPreferencesKey()
val TOKEN = stringPreferencesKey("token")
val EMAIL = stringPreferencesKey("email")

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
            AnNamAppTheme {
                val navController = rememberNavController()
                CardStudyApp(flashCardDao = flashCardDao, navController = navController, networkService = networkService)
            }
        }
    }
}