package com.example.annamapp.room_sqlite_db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize //if app is put to background at any screen, it can then be restored instantly
@Entity(
    tableName = "FlashCards",
    indices = [Index(
        value = ["english_card", "vietnamese_card"],
        unique = true)]
) data class FlashCard(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "english_card") val englishCard: String?,
    @ColumnInfo(name = "vietnamese_card") val vietnameseCard: String?
): Parcelable