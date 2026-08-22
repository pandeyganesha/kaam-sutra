package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pandeyganesha.kaamsutra.data.Task
import com.pandeyganesha.kaamsutra.data.TaskLog
import androidx.compose.foundation.layout.Column


@Composable
fun TodoScreen(activeTodos: List<Task>,
               allTodoLogs: List<TaskLog>,
               onCheckedChange: (Boolean, Task) -> Unit,
               onEditClicked: (Task) -> Unit,
               onDeleteClicked: (Task) -> Unit,
               modifier: Modifier
) {
    if (activeTodos.isEmpty()) {
        EmptyState(screen = Screen.TODO, modifier = modifier)
        return
    }
    Column(modifier = modifier.fillMaxSize()) {
        activeTodos.forEach { todo ->
            TaskRow(
                taskName = todo.name,
                points = todo.pointsDelta,
                isChecked = allTodoLogs.any { it.taskId == todo.id && it.completed},
                onCheckedChange = { checked -> onCheckedChange(checked, todo) },
                onEditClick = { onEditClicked(todo) },
                onDeleteClick = { onDeleteClicked(todo) }
            )
        }
    }
}