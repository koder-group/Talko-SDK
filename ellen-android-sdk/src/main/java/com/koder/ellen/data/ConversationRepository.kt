package com.koder.ellen.data

import com.koder.ellen.model.ClientConfiguration
import com.koder.ellen.model.Conversation
import com.koder.ellen.model.EllenUser

class ConversationRepository(val dataSource: ConversationDataSource) {
    val TAG = "ConversationsRepository"

    fun getClientConfig(): Result<ClientConfiguration> {
        val result = dataSource.getClientConfig()

        if(result is Result.Success) {
//            setClientConfig(result.data)
        }

        return result
    }

    fun registerNotificationToken() {
        dataSource.registerNotificationToken()
    }

    fun getCurrentUser(): Result<EllenUser> {
        val result = dataSource.getCurrentUser()

        if(result is Result.Success) setCurrentUser(result.data)

        return result
    }

    fun getConversations(): MutableList<Conversation> {
        return dataSource.getConversations()
    }

    fun getConversationMessages(conversations: MutableList<Conversation>): MutableList<Conversation> {
        return dataSource.getConversationMessages(conversations)
    }

    fun deleteConversation(conversationId: String): Result<Boolean> {
        return dataSource.deleteConversation(conversationId)
    }

    fun getConversation(conversationId: String): Result<Conversation> {
        return dataSource.getConversation(conversationId)
    }

    private fun setClientConfig(clientConfig: ClientConfiguration) {
//        prefs.saveClientConfig(clientConfig)
    }

    private fun setCurrentUser(currentUser: EllenUser) {
//        prefs.saveCurrentUser(currentUser)
    }
}