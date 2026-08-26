package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import com.pandeyganesha.kaamsutra.data.Todo
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import sh.calvin.reorderable.ReorderableItem

@Composable
fun TodoScreen(activeTodos: List<Todo>,
               onCheckedChange: (Boolean, Todo) -> Unit,
               onEditClicked: (Todo) -> Unit,
               onDeleteClicked: (Todo) -> Unit,
               modifier: Modifier
) {
    val lazyListState = rememberLazyListState()
    var todosNotDone by remember(activeTodos) {
        mutableStateOf(activeTodos.filter { !it.completed })
    }
    val todosDone = activeTodos.filter { it.completed }

    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        todosNotDone = todosNotDone.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    LazyColumn(state = lazyListState, modifier = modifier.fillMaxSize()) {
        items(todosNotDone, key = {it.id} ) { todo ->
            ReorderableItem(reorderableState, key = todo.id) { isDragging ->
                TaskRow(
                    taskName = todo.name,
                    isChecked = todo.completed,
                    onCheckedChange = { checked -> onCheckedChange(checked, todo) },
                    onEditClick = { onEditClicked(todo) },
                    onDeleteClick = { onDeleteClicked(todo) },
                    modifier = Modifier.longPressDraggableHandle()
                )
            }
        }
        items(todosDone, key = {it.id}) { todo ->
            TaskRow(
                taskName = todo.name,
                isChecked = todo.completed,
                onCheckedChange = { checked -> onCheckedChange(checked, todo) },
                onEditClick = { onEditClicked(todo) },
                onDeleteClick = { onDeleteClicked(todo) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(75.dp))
        }
    }
}