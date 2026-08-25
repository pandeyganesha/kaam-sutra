package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import com.pandeyganesha.kaamsutra.data.Goal


@Composable
fun GoalScreen(activeGoals: List<Goal>,
               onCheckedChange: (Boolean, Goal) -> Unit,
               onEditClicked: (Goal) -> Unit,
               onDeleteClicked: (Goal) -> Unit,
               modifier: Modifier
) {
    val (goalsDone, goalsNotDone) = activeGoals.partition { it.completed }
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        goalsNotDone.forEach { goal ->
            TaskRow(
                taskName = goal.name,
                isChecked = goal.completed,
                onCheckedChange = { checked -> onCheckedChange(checked, goal) },
                onEditClick = { onEditClicked(goal) },
                onDeleteClick = { onDeleteClicked(goal) }
            )
        }
        goalsDone.forEach { goal ->
            TaskRow(
                taskName = goal.name,
                isChecked = goal.completed,
                onCheckedChange = { checked -> onCheckedChange(checked, goal) },
                onEditClick = { onEditClicked(goal) },
                onDeleteClick = { onDeleteClicked(goal) }
            )
        }
        Spacer(modifier = Modifier.height(75.dp))
    }
}