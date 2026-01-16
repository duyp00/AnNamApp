package com.example.annamapp.room_sqlite_db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface FlashCardDao {
    @Query("SELECT * FROM FlashCards")
    suspend fun getAll(): List<FlashCard>

    //@Query("SELECT * FROM FlashCards WHERE uid IN (:flashCardIds)")
    //suspend fun loadAllByIds(flashCardIds: IntArray): List<FlashCard>

    //@Query("SELECT * FROM FlashCards WHERE uid = :uid LIMIT 1")
    //suspend fun getCardById(uid: Int): FlashCard?

    @Query("SELECT * FROM FlashCards WHERE english_card LIKE :english AND " +/*'+' here is concatenation, nothing to do with sql*/
            "vietnamese_card LIKE :vietnamese LIMIT 1")
    suspend fun findByCards(english: String, vietnamese: String): FlashCard?

    //if want to match either one of the two fields, simply change AND to OR
    @Query(
        "SELECT * FROM FlashCards WHERE " +
                "(:englishEnabled = 0 OR " +
                "(CASE WHEN :englishWholeWord = 1 THEN english_card = :englishQuery ELSE english_card LIKE '%' || :englishQuery || '%' END)) AND " +
                "(:vietnameseEnabled = 0 OR " +
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

    @Insert(onConflict = OnConflictStrategy.IGNORE, entity = FlashCard::class)
    suspend fun insertAll(vararg flashCard: FlashCard)

    /**
     * Deletes a card.
     * Making this a 'suspend' function to run it off the main thread.
     */
    @Delete(entity = FlashCard::class)
    suspend fun delete(flashCard: FlashCard)

    @Update(entity = FlashCard::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateCard(flashCard: FlashCard)

    @Query("SELECT * FROM FlashCards ORDER BY RANDOM() LIMIT :size")
    suspend fun getLesson(size: Int): List<FlashCard>
}