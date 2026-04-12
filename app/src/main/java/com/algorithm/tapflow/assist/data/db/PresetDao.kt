package com.algorithm.tapflow.assist.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.algorithm.tapflow.assist.data.db.entity.TouchPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {

    @Query("SELECT * FROM touch_presets ORDER BY id DESC")
    fun observePresets(): Flow<List<TouchPresetEntity>>

    @Query("SELECT * FROM touch_presets WHERE id = :id LIMIT 1")
    suspend fun getPresetById(id: Long): TouchPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(entity: TouchPresetEntity): Long

    @Update
    suspend fun updatePreset(entity: TouchPresetEntity)

    @Delete
    suspend fun deletePreset(entity: TouchPresetEntity)

    @Query("DELETE FROM touch_presets WHERE id = :id")
    suspend fun deletePresetById(id: Long)
}