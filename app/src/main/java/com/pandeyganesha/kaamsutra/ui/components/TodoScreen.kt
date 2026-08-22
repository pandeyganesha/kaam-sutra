package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pandeyganesha.kaamsutra.data.Task
import com.pandeyganesha.kaamsutra.data.TaskLog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@Composable
fun TodoScreen(activeTodos: List<Task>,
               allTodoLogs: List<TaskLog>,
               onCheckedChange: (Boolean, Task) -> Unit,
               onEditClicked: (Task) -> Unit,
               onDeleteClicked: (Task) -> Unit,
               modifier: Modifier
) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        activeTodos.forEach { todo ->
            TaskRow(
                taskName = todo.name,
                isChecked = allTodoLogs.any { it.taskId == todo.id && it.completed},
                onCheckedChange = { checked -> onCheckedChange(checked, todo) },
                onEditClick = { onEditClicked(todo) },
                onDeleteClick = { onDeleteClicked(todo) }
            )
        }
    }
}