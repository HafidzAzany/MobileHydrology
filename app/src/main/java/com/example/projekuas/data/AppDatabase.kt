package com.example.projekuas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailyProgressEntity::class, DrinkLogEntity::class],
    version = 2, // naikkan versinya (misal dari 1 ke 2)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dailyProgressDao(): DailyProgressDao
    abstract fun drinkLogDao(): DrinkLogDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hydrology_db"
                )
                    .fallbackToDestructiveMigration() // sementara biar gak crash
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
