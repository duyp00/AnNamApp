package com.example.annamapp.room_sqlite_db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
//import androidx.room.RawQuery
//import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface FlashCardDao {
    //@RawQuery
    //fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int

    @Query("SELECT * FROM FlashCards")
    suspend fun getAll(): List<FlashCard>

    //@Query("SELECT * FROM FlashCards WHERE uid IN (:flashCardIds)")
    //suspend fun loadAllByIds(flashCardIds: IntArray): List<FlashCard>

    //@Query("SELECT * FROM FlashCards WHERE uid = :uid LIMIT 1")
    //suspend fun getCardById(uid: Int): FlashCard?

    /*@Query(
        "UPDATE FlashCards SET english_card = :englishNew, vietnamese_card = :vietnameseNew " +
                "WHERE english_card = :englishOld AND vietnamese_card = :vietnameseOld"
    )
    suspend fun updateFlashCard(
        englishOld: String, vietnameseOld: String,
        englishNew: String, vietnameseNew: String
    )

    @Query(
        "DELETE FROM FlashCards WHERE english_card = :english " +
                "AND vietnamese_card = :vietnamese "
    ) suspend fun deleteFlashCard(english: String, vietnamese: String)*/

    @Query("SELECT * FROM FlashCards WHERE english_card LIKE :english AND " +/*'+' here is concatenation, nothing to do with sql*/
            "vietnamese_card LIKE :vietnamese LIMIT 1")
    suspend fun findByCards(english: String, vietnamese: String): FlashCard?

    //change between AND and OR to match both or either one of the 2 fields, and ...Enabled = 0 / 1 and AND / OR
    @Query(
        "SELECT * FROM FlashCards WHERE " +
                "(:englishEnabled = 1 AND " +
                "(CASE WHEN :englishWholeWord = 1 THEN english_card = :englishQuery ELSE english_card LIKE '%' || :englishQuery || '%' END)) OR " +
                "(:vietnameseEnabled = 1 AND " +
                "(CASE WHEN :vietnameseWholeWord = 1 THEN vietnamese_card = :vietnameseQuery ELSE vietnamese_card LIKE '%' || :vietnameseQuery || '%' END))"
    )
    suspend fun searchCards(
        englishQuery: String,
        englishEnabled: Boolean,
        englishWholeWord: Boolean,
        vietnameseQuery: String,
        vietnameseEnabled: Boolean,
        vietnameseWholeWord: Boolean
    ): List<FlashCard>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg flashCard: FlashCard)

    /**
     * Deletes a card.
     * Making this a 'suspend' function to run it off the main thread.
     */
    @Delete
    suspend fun delete(flashCard: FlashCard)

    @Update(onConflict = OnConflictStrategy.ABORT) //IGNORE is good, but ABORT is used to capture conflicts
    suspend fun updateCard(flashCard: FlashCard)

    //@Query("SELECT * FROM FlashCards LIMIT :size")
    @Query("SELECT * FROM FlashCards ORDER BY RANDOM() LIMIT :size")
    suspend fun getLesson(size: Int): List<FlashCard>
}