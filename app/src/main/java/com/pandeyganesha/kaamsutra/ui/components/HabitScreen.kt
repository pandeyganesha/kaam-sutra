package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pandeyganesha.kaamsutra.data.Habit
import com.pandeyganesha.kaamsutra.data.HabitLog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp


@Composable
fun HabitScreen(activeHabits: List<Habit>,
                allHabitLogsForToday: List<HabitLog>,
                onCheckedChange: (Boolean, Habit) -> Unit,
                onEditClicked: (Habit) -> Unit,
                onDeleteClicked: (Habit) -> Unit,
                modifier: Modifier
                ) {

    val doneHabits = activeHabits.filter { habit ->
        allHabitLogsForToday.any { log ->
            log.habitId == habit.id && log.completed
        }
    }

    val notDoneHabits = activeHabits.filter { habit ->
        allHabitLogsForToday.none { log ->
            log.habitId == habit.id && log.completed
        }
    }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        notDoneHabits.forEach { habit ->
            TaskRow(
                taskName = habit.name,
                isChecked = allHabitLogsForToday.any { it.habitId == habit.id && it.completed},
                onCheckedChange = { checked -> onCheckedChange(checked, habit) },
                onEditClick = { onEditClicked(habit) },
                onDeleteClick = { onDeleteClicked(habit) }
            )
        }
        doneHabits.forEach { habit ->
            TaskRow(
                taskName = habit.name,
                isChecked = allHabitLogsForToday.any { it.habitId == habit.id && it.completed},
                onCheckedChange = { checked -> onCheckedChange(checked, habit) },
                onEditClick = { onEditClicked(habit) },
                onDeleteClick = { onDeleteClicked(habit) }
            )
        }
        Spacer(modifier = Modifier.height(75.dp))
    }
}