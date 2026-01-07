package com.example.projekuas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_progress")
data class DailyProgressEntity(
    @PrimaryKey val dateKey: String, // contoh: "2026-01-07"
    val targetMl: Int,
    val currentMl: Int
)
