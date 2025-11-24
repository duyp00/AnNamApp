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

    @Query("SELECT * FROM FlashCards WHERE uid IN (:flashCardIds)")
    suspend fun loadAllByIds(flashCardIds: IntArray): List<FlashCard>?

    /**
     * Finds a single card by its unique ID (uid), used for the detail/delete screen.
     */
    @Query("SELECT * FROM FlashCards WHERE uid = :uid LIMIT 1")
    suspend fun getCardById(uid: Int?): FlashCard?

    @Query("SELECT * FROM FlashCards WHERE english_card LIKE :english AND " +/*'+' here is concatenation, nothing to do with sql*/
            "vietnamese_card LIKE :vietnamese LIMIT 1")
    suspend fun findByCards(english: String, vietnamese: String): FlashCard?

    /**
     * Search cards with partial match on both fields (OR logic).
     * Returns cards where either English OR Vietnamese field matches.
     * Empty queries are ignored.
     */
    @Query("SELECT * FROM FlashCards WHERE " +
            "(:englishQuery != '' AND english_card LIKE '%' || :englishQuery || '%') OR " +
            "(:vietnameseQuery != '' AND vietnamese_card LIKE '%' || :vietnameseQuery || '%')")
    suspend fun searchCardsPartial(
        englishQuery: String,
        vietnameseQuery: String
    ): List<FlashCard>

    /**
     * Search cards with exact English, partial Vietnamese (OR logic).
     * Returns cards where either English OR Vietnamese field matches.
     * Empty queries are ignored.
     */
    @Query("SELECT * FROM FlashCards WHERE " +
            "(:englishQuery != '' AND english_card = :englishQuery) OR " +
            "(:vietnameseQuery != '' AND vietnamese_card LIKE '%' || :vietnameseQuery || '%')")
    suspend fun searchCardsExactEnglish(
        englishQuery: String,
        vietnameseQuery: String
    ): List<FlashCard>

    /**
     * Search cards with partial English, exact Vietnamese (OR logic).
     * Returns cards where either English OR Vietnamese field matches.
     * Empty queries are ignored.
     */
    @Query("SELECT * FROM FlashCards WHERE " +
            "(:englishQuery != '' AND english_card LIKE '%' || :englishQuery || '%') OR " +
            "(:vietnameseQuery != '' AND vietnamese_card = :vietnameseQuery)")
    suspend fun searchCardsExactVietnamese(
        englishQuery: String,
        vietnameseQuery: String
    ): List<FlashCard>

    /**
     * Search cards with exact match on both fields (OR logic).
     * Returns cards where either English OR Vietnamese field matches.
     * Empty queries are ignored.
     */
    @Query("SELECT * FROM FlashCards WHERE " +
            "(:englishQuery != '' AND english_card = :englishQuery) OR " +
            "(:vietnameseQuery != '' AND vietnamese_card = :vietnameseQuery)")
    suspend fun searchCardsExactBoth(
        englishQuery: String,
        vietnameseQuery: String
    ): List<FlashCard>

    @Insert(onConflict = OnConflictStrategy.IGNORE, entity = FlashCard::class)
    suspend fun insertAll(vararg flashCard: FlashCard)

    /**
     * Deletes a card.
     * Making this a 'suspend' function to run it off the main thread.
     */
    @Delete(entity = FlashCard::class)
    suspend fun delete(flashCard: FlashCard)

    @Update(entity = FlashCard::class)
    suspend fun updateCard(flashCard: FlashCard)
}