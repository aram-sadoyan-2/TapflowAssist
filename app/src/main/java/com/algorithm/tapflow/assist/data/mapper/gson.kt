package com.algorithm.tapflow.assist.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.algorithm.tapflow.assist.data.db.entity.TouchPresetEntity
import com.algorithm.tapflow.assist.data.model.GestureType
import com.algorithm.tapflow.assist.data.model.TouchPoint
import com.algorithm.tapflow.assist.data.model.TouchPreset

private val gson = Gson()

fun TouchPresetEntity.toDomain(): TouchPreset {
    val listType = object : TypeToken<List<TouchPoint>>() {}.type
    val points: List<TouchPoint> = gson.fromJson(pointsJson, listType) ?: emptyList()

    return TouchPreset(
        id = id,
        name = name,
        type = GestureType.valueOf(type),
        intervalMs = intervalMs,
        repeatCount = repeatCount,
        holdDurationMs = holdDurationMs,
        points = points
    )
}

fun TouchPreset.toEntity(): TouchPresetEntity {
    return TouchPresetEntity(
        id = id,
        name = name,
        type = type.name,
        intervalMs = intervalMs,
        repeatCount = repeatCount,
        holdDurationMs = holdDurationMs,
        pointsJson = gson.toJson(points)
    )
}