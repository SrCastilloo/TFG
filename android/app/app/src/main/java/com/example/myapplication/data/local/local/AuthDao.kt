package com.example.myapplication.data.local.auth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: AppUserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: AuthSessionEntity)

    @Query("DELETE FROM auth_session WHERE id = 'current'")
    suspend fun clearSession()

    @Query("SELECT COUNT(*) FROM app_users")
    suspend fun countUsers(): Int

    @Query("SELECT * FROM app_users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): AppUserEntity?

    @Query("SELECT * FROM app_users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): AppUserEntity?

    @Query(
        """
        SELECT u.*
        FROM app_users u
        INNER JOIN auth_session s ON u.id = s.userId
        WHERE s.id = 'current'
        LIMIT 1
        """
    )
    fun observeCurrentUser(): Flow<AppUserEntity?>

    @Query("SELECT * FROM app_users ORDER BY createdAtMillis ASC")
    fun observeAllUsers(): Flow<List<AppUserEntity>>
}