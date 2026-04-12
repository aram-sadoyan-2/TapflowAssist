package com.algorithm.tapflow.assist.data.repository

import com.algorithm.tapflow.assist.data.model.TouchPreset
import kotlinx.coroutines.flow.Flow

interface PresetRepository {
    fun observePresets(): Flow<List<TouchPreset>>
    suspend fun getPresetById(id: Long): TouchPreset?
    suspend fun savePreset(preset: TouchPreset): Long
    suspend fun deletePreset(id: Long)
}