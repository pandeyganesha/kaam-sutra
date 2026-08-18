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
    Column(modifier = modifier.fillMaxSize()) {
        activeHabits.forEach { habit ->
            TaskRow(
                taskName = habit.name,
                isChecked = allHabitLogsForToday.any { it.taskId == habit.id },
                onCheckedChange = { checked -> onCheckedChange(checked, habit) },
                onEditClick = { onEditClicked(habit) },
                onDeleteClick = { onDeleteClicked(habit) }
            )
        }
    }
}