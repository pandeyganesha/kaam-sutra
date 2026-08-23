package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pandeyganesha.kaamsutra.data.Task
import com.pandeyganesha.kaamsutra.data.TaskLog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp


@Composable
fun HabitScreen(activeHabits: List<Task>,
                allHabitLogsForToday: List<TaskLog>,
                onCheckedChange: (Boolean, Task) -> Unit,
                onEditClicked: (Task) -> Unit,
                onDeleteClicked: (Task) -> Unit,
                modifier: Modifier
                ) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        activeHabits.forEach { habit ->
            TaskRow(
                taskName = habit.name,
                isChecked = allHabitLogsForToday.any { it.taskId == habit.id && it.completed},
                onCheckedChange = { checked -> onCheckedChange(checked, habit) },
                onEditClick = { onEditClicked(habit) },
                onDeleteClick = { onDeleteClicked(habit) }
            )
        }
        Spacer(modifier = Modifier.height(75.dp))
    }
}