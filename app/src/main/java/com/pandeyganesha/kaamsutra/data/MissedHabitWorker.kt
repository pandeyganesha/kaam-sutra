package com.pandeyganesha.kaamsutra.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.pandeyganesha.kaamsutra.ui.components.Screen
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class MissedHabitWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = DatabaseProvider.getDatabase(applicationContext)
        val taskDao = db.taskDao()
        val habitLogDao = db.taskLogDao()

        val dayJustEnded = LocalDate.now().minusDays(1).toString()

        val activeHabits = taskDao.getActiveTasksOnce(Screen.HABITS)
        val yesterdaysLogs = habitLogDao.getLogsForDateOnce(dayJustEnded)

        val missedHabits = activeHabits.filter { habit ->
            yesterdaysLogs.none { it.taskId == habit.id }
        }

        missedHabits.forEach { habit ->
            habitLogDao.insertLog(
                TaskLog(
                    taskId = habit.id,
                    date = dayJustEnded,
                    completed = false
                )
            )
        }

        return Result.success()
    }
}

fun scheduleMissedHabitSettlement(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<MissedHabitWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(calculateDelayUntil(0, 5), TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "missed_habit_settlement",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}