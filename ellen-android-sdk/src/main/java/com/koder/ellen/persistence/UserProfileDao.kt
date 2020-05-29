package com.koder.ellen.persistence

import androidx.room.*

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM UserProfile")
    fun getAll(): List<UserProfile>

    @Query("SELECT * FROM UserProfile WHERE user_id == :userId")
    fun getUserProfile(userId: String): UserProfile

    @Update
    fun update(user: UserProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserProfile)
}