package com.example.annamapp.room_sqlite_db

import androidx.room.Database
import androidx.room.RoomDatabase
//import android.content.Context
//import androidx.room.Room

@Database(entities = [FlashCard::class], version = 1)
abstract class AnNamDatabase: RoomDatabase() {
    abstract fun flashCardDao(): FlashCardDao
    /*companion object {
        @Volatile // Ensures visibility to all threads
        private var INSTANCE: AnNamDatabase? = null
        fun getDatabase(context: Context): AnNamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Use application context to prevent memory leaks
                    AnNamDatabase::class.java,
                    "AnNamFlashCardDb"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }*/
}