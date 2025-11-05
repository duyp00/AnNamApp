package com.example.annamapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FlashCard::class], version = 1)
abstract class AnNamDatabase : RoomDatabase() {
    abstract fun flashCardDao(): FlashCardDao
}