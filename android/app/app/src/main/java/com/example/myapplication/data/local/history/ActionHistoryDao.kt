package com.example.myapplication.data.local.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionHistoryDao {

    @Query(
        """
        SELECT * 
        FROM action_history 
        ORDER BY timestampMillis DESC 
        LIMIT :limit
        """
    )
    fun observeRecent(limit: Int): Flow<List<ActionHistoryEntity>>

    @Query(
        """
        SELECT * 
        FROM action_history 
        ORDER BY timestampMillis DESC
        """
    )
    fun observeAll(): Flow<List<ActionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ActionHistoryEntity)

    @Query("DELETE FROM action_history")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM action_history")
    fun observeTotalCount(): Flow<Int>
}