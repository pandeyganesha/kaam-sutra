package com.pandeyganesha.kaamsutra.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Entity(tableName = "habit_log")
data class HabitLog(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val date: String,
    val pointsAwarded: Int
)

@Dao
interface HabitLogDao {

    @Insert
    suspend fun insertHabitLog(habitLog: HabitLog)

    @Query("SELECT SUM(pointsAwarded) FROM habit_log")
    fun getNetWorth(): Flow<Int?>

    @Query("SELECT * from habit_log where date = :date")
    fun getLogsForDate(date: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_log WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getLogForHabitAndDateOnce(habitId: String, date: String): HabitLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLog)

    suspend fun upsertLog(newLog: HabitLog) {
        val existing = getLogForHabitAndDateOnce(newLog.habitId, newLog.date)
        val logToSave = if (existing != null) newLog.copy(id = existing.id) else newLog
        insertLog(logToSave)
    }

    @Query("SELECT * FROM habit_log WHERE date = :date")
    suspend fun getLogsForDateOnce(date: String): List<HabitLog>
}