package com.koder.ellen.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ConversationDao {
    @Query("SELECT * FROM Conversation")
    fun getAll(): List<Conversation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(conversation: Conversation)
}