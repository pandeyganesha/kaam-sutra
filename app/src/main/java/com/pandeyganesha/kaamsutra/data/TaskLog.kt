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

@Entity(tableName = "task_log")
data class TaskLog(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val date: String,
)

@Dao
interface TaskLogDao {

    @Insert
    suspend fun insertTaskLog(taskLog: TaskLog)

    @Query("SELECT * from task_log where date = :date")
    fun getLogsForDate(date: String): Flow<List<TaskLog>>

    @Query("SELECT * FROM task_log WHERE taskId = :taskId AND date = :date LIMIT 1")
    suspend fun getLogForTaskAndDateOnce(taskId: String, date: String): TaskLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TaskLog)

    suspend fun upsertLog(newLog: TaskLog) {
        val existing = getLogForTaskAndDateOnce(newLog.taskId, newLog.date)
        val logToSave = if (existing != null) newLog.copy(id = existing.id) else newLog
        insertLog(logToSave)
    }

    @Query("SELECT * FROM task_log WHERE date = :date")
    suspend fun getLogsForDateOnce(date: String): List<TaskLog>
}