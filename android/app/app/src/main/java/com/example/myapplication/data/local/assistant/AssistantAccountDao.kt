package com.example.myapplication.data.local.assistant

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantAccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAccount(account: AssistantAccountEntity)

    @Query(
        """
        SELECT *
        FROM assistant_accounts
        WHERE userId = :userId
        LIMIT 1
        """
    )
    fun observeAccountForUser(userId: String): Flow<AssistantAccountEntity?>

    @Query(
        """
        SELECT *
        FROM assistant_accounts
        WHERE userId = :userId
        LIMIT 1
        """
    )
    suspend fun getAccountForUser(userId: String): AssistantAccountEntity?

    @Query(
        """
        SELECT *
        FROM assistant_accounts
        ORDER BY updatedAtMillis DESC
        """
    )
    fun observeAllAccounts(): Flow<List<AssistantAccountEntity>>

    @Query(
        """
        DELETE FROM assistant_accounts
        WHERE userId = :userId
        """
    )
    suspend fun deleteAccountForUser(userId: String)
}