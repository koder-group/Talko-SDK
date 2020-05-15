package com.koder.ellen.persistence

import androidx.room.*

@Dao
interface MessageDao {
    @Query("SELECT * FROM Message")
    fun getAll(): List<Message>

    @Query("SELECT * FROM Message WHERE conversation_id == :conversationId ORDER BY message_ts DESC")
    fun getMessages(conversationId: String): List<Message>

    @Query("SELECT * FROM Message WHERE message_id == :messageId")
    fun getMessage(messageId: String): Message

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(message: Message)

    @Update
    fun update(message: Message)

    @Query("DELETE FROM Message WHERE conversation_id == :conversationId")
    fun deleteMessages(conversationId: String)

    @Query("DELETE FROM Message WHERE message_id == :messageId")
    fun deleteMessage(messageId: String)

    @Query("DELETE FROM Message")
    fun deleteAll()
}