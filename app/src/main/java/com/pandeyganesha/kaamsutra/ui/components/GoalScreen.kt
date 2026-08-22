package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pandeyganesha.kaamsutra.data.Task
import com.pandeyganesha.kaamsutra.data.TaskLog
import androidx.compose.foundation.layout.Column


@Composable
fun GoalScreen(activeGoals: List<Task>,
                allGoalLogs: List<TaskLog>,
                onCheckedChange: (Boolean, Task) -> Unit,
                onEditClicked: (Task) -> Unit,
                onDeleteClicked: (Task) -> Unit,
                modifier: Modifier
) {
    if (activeGoals.isEmpty()) {
        EmptyState(screen = Screen.GOALS, modifier = modifier)
        return
    }
    Column(modifier = modifier.fillMaxSize()) {
        activeGoals.forEach { goal ->
            TaskRow(
                taskName = goal.name,
                points = goal.pointsDelta,
                isChecked = allGoalLogs.any { it.taskId == goal.id && it.completed},
                onCheckedChange = { checked -> onCheckedChange(checked, goal) },
                onEditClick = { onEditClicked(goal) },
                onDeleteClick = { onDeleteClicked(goal) }
            )
        }
    }
}