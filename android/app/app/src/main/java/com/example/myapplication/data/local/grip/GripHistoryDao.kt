package com.example.myapplication.data.local.grip_history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GripHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GripHistoryEntity)

    @Query(
        """
        SELECT *
        FROM grip_history
        ORDER BY timestampMillis DESC
        LIMIT :limit
        """
    )
    fun observeRecent(limit: Int): Flow<List<GripHistoryEntity>>

    @Query(
        """
        SELECT *
        FROM grip_history
        ORDER BY timestampMillis DESC
        """
    )
    fun observeAll(): Flow<List<GripHistoryEntity>>

    @Query("DELETE FROM grip_history")
    suspend fun clear()
}