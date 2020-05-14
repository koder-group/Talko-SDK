package com.koder.ellen.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {
    @Query("SELECT * FROM Message")
    fun getAll(): List<Message>

    @Query("SELECT * FROM Message WHERE conversation_id == :conversationId ORDER BY message_ts DESC")
    fun getMessages(conversationId: String): List<Message>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(message: Message)

    @Query("DELETE FROM Message WHERE conversation_id == :conversationId")
    fun deleteMessages(conversationId: String)

    @Query("DELETE FROM Message WHERE message_id == :messageId")
    fun deleteMessage(messageId: String)

    @Query("DELETE FROM Message")
    fun deleteAll()
}