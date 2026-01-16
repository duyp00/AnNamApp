package com.example.annamapp
/*
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.annamapp.room_sqlite_db.AnNamDatabase
import com.example.annamapp.room_sqlite_db.FlashCard
import com.example.annamapp.room_sqlite_db.FlashCardDao
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
            flashCardDao.insertAll(flashCard)
        }

        val item: FlashCard?
        val item1: FlashCard?
        runBlocking {
            item = flashCardDao.findByCards("test_english", "test_vietnamese")
            item1 = flashCardDao.getCardById(item?.uid)
        }
        assertEquals(flashCard.englishCard, item1?.englishCard)
        assertEquals(flashCard.vietnameseCard, item1?.vietnameseCard)
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
            flashCardDao.insertAll(flashCard)
        }
        var error = false
        runBlocking {
            try {
                flashCardDao.insertAll(flashCard)
            } catch (e: SQLiteConstraintException){
                error = true
            }
        }
        assertEquals(false, error)
    }

    /* Delete */
    @Test
    fun deleteExistingFlashCard() {
        val flashCard =
            FlashCard(
                uid = 0,
                englishCard = "test_english",
                vietnameseCard = "test_vietnamese"
            )

        var flashCardsBefore: List<FlashCard>
        runBlocking {
            flashCardsBefore = flashCardDao.getAll()
        }
        runBlocking{
            flashCardDao.insertAll(flashCard)
            val insertedCard = flashCardDao.findByCards("test_english", "test_vietnamese")
            flashCardDao.delete(insertedCard!!)
        }
        var flashCardsAfter: List<FlashCard>
        runBlocking {
            flashCardsAfter = flashCardDao.getAll()
        }
        assertEquals(flashCardsBefore, flashCardsAfter)
    }


    @Test
    fun deleteNonExistingFlashCard() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AnNamDatabase::class.java).build()
        flashCardDao = db.flashCardDao()

        val flashCard =
            FlashCard(
                uid = 0,
                englishCard = "test_english",
                vietnameseCard = "test_vietnamese"
            )

        var flashCardsBefore: List<FlashCard>
        runBlocking {
            flashCardDao.insertAll(flashCard)
            flashCardsBefore = flashCardDao.getAll()
        }
        runBlocking {
            flashCardDao.delete(FlashCard(
                uid = 0,
                englishCard = "test_english_1",
                vietnameseCard = "test_vietnamese_1"
            )
            )
        }
        var flashCardsAfter: List<FlashCard>
        runBlocking {
            flashCardsAfter = flashCardDao.getAll()
        }
        assertEquals(flashCardsBefore, flashCardsAfter)
    }
    /* Similar for the other 2 cases */

}
*/