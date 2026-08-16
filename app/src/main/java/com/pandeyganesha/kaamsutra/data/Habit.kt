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


@Entity(tableName = "habit")
data class Habit(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val worthDelta: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Update
    suspend fun softDeleteHabit(habit: Habit)

    @Query("SELECT * from habit order by createdAt")
    fun getHabits(): Flow<List<Habit>>

    @Query("Select * from habit where id = :habitId")
    fun getHabit(habitId: String): Flow<Habit?>

    @Query("SELECT * from habit where isActive = 1 order by createdAt")
    fun getActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habit WHERE isActive = 1")
    suspend fun getActiveHabitsOnce(): List<Habit>
}