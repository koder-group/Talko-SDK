package com.koder.ellen.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Conversation::class, Message::class, UserProfile::class], version = 1)
abstract class TalkoDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile private var INSTANCE: TalkoDatabase? = null

        fun getInstance(context: Context): TalkoDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                TalkoDatabase::class.java, "talko_db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
    }
}
