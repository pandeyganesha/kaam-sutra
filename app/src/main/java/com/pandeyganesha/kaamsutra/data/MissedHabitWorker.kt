package com.pandeyganesha.kaamsutra.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class MissedHabitWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = DatabaseProvider.getDatabase(applicationContext)
        val habitDao = db.habitDao()
        val habitLogDao = db.habitLogDao()

        val dayJustEnded = LocalDate.now().minusDays(1).toString()

        val activeHabits = habitDao.getActiveHabitsOnce()
        val yesterdaysLogs = habitLogDao.getLogsForDateOnce(dayJustEnded)

        val missedHabits = activeHabits.filter { habit ->
            yesterdaysLogs.none { it.habitId == habit.id && it.pointsAwarded > 0 }
        }

        missedHabits.forEach { habit ->
            habitLogDao.insertLog(
                HabitLog(
                    habitId = habit.id,
                    date = dayJustEnded,
                    pointsAwarded = -habit.worthDelta
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