package com.example.annamapp

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class DaoTest {
    @get:Rule
    private lateinit var db: AnNamDatabase
    private lateinit var flashCardDao: FlashCardDao


    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AnNamDatabase::class.java).build()
        flashCardDao = db.flashCardDao()
    }

    @After
    fun close(){
        db.close()
    }


    @Test
    fun insertFlashCardSuccessful() {
        val flashCard =
            FlashCard(
                uid = 0,
                englishCard = "test_english",
                vietnameseCard = "test_vietnamese"
            )

        runBlocking {
            flashCardDao.insertCard(flashCard)
        }

        val item:FlashCard
        runBlocking {
            item = flashCardDao.findByCards("test_english", "test_vietnamese")!!
        }
        assertEquals(flashCard.englishCard, item.englishCard, )
        assertEquals(flashCard.vietnameseCard, item.vietnameseCard)
    }

    @Test
    fun insertFlashCardUnSuccessful() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AnNamDatabase::class.java).build()
        flashCardDao = db.flashCardDao()

        val flashCard =
            FlashCard(
                uid = 0,
                englishCard = "test_english",
                vietnameseCard = "test_vietnamese"
            )

        runBlocking {
            flashCardDao.insertCard(flashCard)
        }
        var error = false
        runBlocking {
            try {
                flashCardDao.insertCard(flashCard)
            } catch (e: SQLiteConstraintException){
                error = true
            }
        }
        assertEquals(false, error)
    }
}