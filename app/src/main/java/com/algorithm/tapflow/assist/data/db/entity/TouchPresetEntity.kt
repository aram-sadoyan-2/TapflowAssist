package com.algorithm.tapflow.assist.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "touch_presets")
data class TouchPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val intervalMs: Long,
    val repeatCount: Int,
    val holdDurationMs: Long,
    val pointsJson: String
)