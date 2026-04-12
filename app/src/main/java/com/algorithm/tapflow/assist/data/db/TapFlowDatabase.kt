package com.algorithm.tapflow.assist.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.algorithm.tapflow.assist.data.db.entity.TouchPresetEntity

@Database(
    entities = [TouchPresetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TapFlowDatabase : RoomDatabase() {
    abstract fun presetDao(): PresetDao
}