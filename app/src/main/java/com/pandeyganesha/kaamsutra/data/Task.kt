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
import com.pandeyganesha.kaamsutra.ui.components.Screen


@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val taskType: Screen,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("UPDATE tasks SET isActive = 0 WHERE id = :taskId")
    suspend fun softDeleteTask(taskId: String)

    @Query("SELECT * from tasks order by createdAt")
    fun getTasks(): Flow<List<Task>>

    @Query("Select * from tasks where id = :taskId")
    fun getTask(taskId: String): Flow<Task?>

    @Query("SELECT * from tasks where isActive = 1 and taskType = :taskType order by createdAt")
    fun getActiveTasks(taskType: Screen): Flow<List<Task>>
}