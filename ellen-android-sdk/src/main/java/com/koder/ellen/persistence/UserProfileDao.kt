package com.koder.ellen.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM UserProfile")
    fun getAll(): List<UserProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserProfile)
}