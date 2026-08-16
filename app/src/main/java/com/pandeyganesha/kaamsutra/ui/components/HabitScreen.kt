package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pandeyganesha.kaamsutra.data.Habit
import com.pandeyganesha.kaamsutra.data.HabitLog
import androidx.compose.foundation.layout.Column


@Composable
fun HabitScreen(activeHabits: List<Habit>,
                allHabitLogsForToday: List<HabitLog>,
                onCheckedChange: (Boolean, Habit) -> Unit,
                onEditClicked: (Habit) -> Unit,
                onDeleteClicked: (Habit) -> Unit,
                modifier: Modifier
                ) {
    Column(modifier = modifier.fillMaxSize()) {
        activeHabits.forEach { habit ->
            HabitRow(
                habitName = habit.name,
                worthDelta = habit.worthDelta,
                isChecked = allHabitLogsForToday.any { it.habitId == habit.id && it.pointsAwarded > 0 },
                onCheckedChange = { checked -> onCheckedChange(checked, habit) },
                onEditClick = { onEditClicked(habit) },
                onDeleteClick = { onDeleteClicked(habit) }
            )
        }
    }
}