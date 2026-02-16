package com.example.annamapp.room_sqlite_db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable //for serialization and deserialization of FlashCard objects, useful for saving to file or sending over network. This allows FlashCard instances to be easily converted to and from formats like JSON, which can be beneficial for data persistence or communication between different parts of the app or with external services.
@Parcelize //allow FlashCard objects to be passed between Android components (e.g., Activities, Fragments) via Intents or Bundles. This is especially useful for passing FlashCard objects as arguments in navigation components or when starting new activities that require FlashCard data.
@Entity(
    tableName = "FlashCards",
    indices = [Index(
        value = ["english_card", "vietnamese_card"],
        unique = true)]
) data class FlashCard(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0L, //auto generated so set 0L as placeholder so no longer need to specify when inserting
    @ColumnInfo(name = "english_card") val englishCard: String?,
    @ColumnInfo(name = "vietnamese_card") val vietnameseCard: String?
): Parcelable