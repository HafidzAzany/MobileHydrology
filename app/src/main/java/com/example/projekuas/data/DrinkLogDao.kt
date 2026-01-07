package com.example.projekuas.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DrinkLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DrinkLogEntity)

    // Hari ini (pakai dateKey)
    @Query("SELECT * FROM drink_log WHERE dateKey = :dateKey ORDER BY timestamp DESC")
    suspend fun getByDate(dateKey: String): List<DrinkLogEntity>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM drink_log WHERE dateKey = :dateKey")
    suspend fun sumByDate(dateKey: String): Int

    // Range dateKey (yyyy-MM-dd) untuk list
    @Query("SELECT * FROM drink_log WHERE dateKey BETWEEN :startKey AND :endKey ORDER BY timestamp DESC")
    suspend fun getBetween(startKey: String, endKey: String): List<DrinkLogEntity>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM drink_log WHERE dateKey BETWEEN :startKey AND :endKey")
    suspend fun sumBetween(startKey: String, endKey: String): Int

    // Grafik 7 hari (total per hari)
    @Query("""
        SELECT dateKey AS dateKey, COALESCE(SUM(amountMl), 0) AS totalMl
        FROM drink_log
        WHERE dateKey BETWEEN :startKey AND :endKey
        GROUP BY dateKey
        ORDER BY dateKey ASC
    """)
    suspend fun sumPerDayBetween(startKey: String, endKey: String): List<DaySum>

    // Range timestamp (buat week/month kalau mau pakai millis)
    @Query("SELECT * FROM drink_log WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getBetweenTime(startTime: Long, endTime: Long): List<DrinkLogEntity>


    // Filter
    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM drink_log WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun sumBetweenTime(startTime: Long, endTime: Long): Int

    @Query("""
    SELECT * FROM drink_log 
    WHERE dateKey = :dateKey 
    ORDER BY timestamp DESC
""")
    suspend fun getByDateNewest(dateKey: String): List<DrinkLogEntity>

    @Query("""
    SELECT * FROM drink_log 
    WHERE dateKey = :dateKey 
    ORDER BY timestamp ASC
""")
    suspend fun getByDateOldest(dateKey: String): List<DrinkLogEntity>

    @Query("""
    SELECT * FROM drink_log 
    WHERE dateKey = :dateKey 
    ORDER BY amountMl DESC, timestamp DESC
""")
    suspend fun getByDateBiggest(dateKey: String): List<DrinkLogEntity>

    @Query("""
    SELECT * FROM drink_log 
    WHERE dateKey = :dateKey 
    ORDER BY amountMl ASC, timestamp DESC
""")
    suspend fun getByDateSmallest(dateKey: String): List<DrinkLogEntity>

}
