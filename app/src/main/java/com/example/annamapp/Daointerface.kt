package com.example.annamapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FlashCardDao {
    @Query("SELECT * FROM FlashCards")
    suspend fun getAll(): List<FlashCard>

    @Query("SELECT * FROM FlashCards WHERE uid IN (:flashCardIds)")
    suspend fun loadAllByIds(flashCardIds: IntArray): List<FlashCard>

    @Query("SELECT * FROM FlashCards WHERE english_card LIKE :english AND " +
            "vietnamese_card LIKE :vietnamese LIMIT 1")
    suspend fun findByCards(english: String, vietnamese: String): FlashCard

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg flashCard: FlashCard)

    @Delete
    fun delete(flashCard: FlashCard)
}