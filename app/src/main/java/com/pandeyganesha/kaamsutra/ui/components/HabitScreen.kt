package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pandeyganesha.kaamsutra.data.Task
import com.pandeyganesha.kaamsutra.data.TaskLog
import androidx.compose.foundation.layout.Column


@Composable
fun HabitScreen(activeHabits: List<Task>,
                allHabitLogsForToday: List<TaskLog>,
                onCheckedChange: (Boolean, Task) -> Unit,
                onEditClicked: (Task) -> Unit,
                onDeleteClicked: (Task) -> Unit,
                modifier: Modifier
                ) {
    if (activeHabits.isEmpty()) {
        EmptyState(screen = Screen.HABITS, modifier = modifier)
        return
    }
    Column(modifier = modifier.fillMaxSize()) {
        activeHabits.forEach { habit ->
            TaskRow(
                taskName = habit.name,
                points = habit.pointsDelta,
                isChecked = allHabitLogsForToday.any { it.taskId == habit.id && it.completed},
                onCheckedChange = { checked -> onCheckedChange(checked, habit) },
                onEditClick = { onEditClicked(habit) },
                onDeleteClick = { onDeleteClicked(habit) }
            )
        }
    }
}