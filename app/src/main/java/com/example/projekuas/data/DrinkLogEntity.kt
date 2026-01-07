package com.example.projekuas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drink_log")
data class DrinkLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateKey: String,        // "yyyy-MM-dd"
    val timeMillis: Long,       // jam kejadian
    val amountMl: Int,
    val source: String,         // "manual" / "auto"
    val timestamp: Long         // untuk query BETWEEN time
)
