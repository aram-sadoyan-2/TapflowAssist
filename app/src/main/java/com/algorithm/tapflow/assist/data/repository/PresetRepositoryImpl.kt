package com.algorithm.tapflow.assist.data.repository

import com.algorithm.tapflow.assist.data.db.PresetDao
import com.algorithm.tapflow.assist.data.mapper.toDomain
import com.algorithm.tapflow.assist.data.mapper.toEntity
import com.algorithm.tapflow.assist.data.model.TouchPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PresetRepositoryImpl(
    private val dao: PresetDao
) : PresetRepository {

    override fun observePresets(): Flow<List<TouchPreset>> {
        return dao.observePresets().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getPresetById(id: Long): TouchPreset? {
        return dao.getPresetById(id)?.toDomain()
    }

    override suspend fun savePreset(preset: TouchPreset): Long {
        return if (preset.id == 0L) {
            dao.insertPreset(preset.toEntity())
        } else {
            dao.updatePreset(preset.toEntity())
            preset.id
        }
    }

    override suspend fun deletePreset(id: Long) {
        dao.deletePresetById(id)
    }
}