package com.koder.ellen.persistence

import androidx.room.*

@Dao
interface ConversationDao {
    @Query("SELECT * FROM Conversation")
    fun getAll(): List<Conversation>

    @Query("SELECT * FROM Conversation WHERE conversation_id == :conversationId")
    fun getConversation(conversationId: String): Conversation

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(conversation: Conversation)

    @Update
    fun update(conversation: Conversation)

    @Query("DELETE FROM Conversation WHERE conversation_id == :conversationId")
    fun deleteConversation(conversationId: String)

    @Query("DELETE FROM Conversation")
    fun deleteAll()
}