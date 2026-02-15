package com.example.annamapp.room_sqlite_db

import androidx.room.Database
import androidx.room.RoomDatabase
//import android.content.Context
//import androidx.room.Room

@Database(entities = [FlashCard::class], version = 1)
abstract class AnNamDatabase: RoomDatabase() {
    abstract fun flashCardDao(): FlashCardDao
}