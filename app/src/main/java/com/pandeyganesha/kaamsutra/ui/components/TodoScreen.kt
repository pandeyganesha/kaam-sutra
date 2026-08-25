package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import com.pandeyganesha.kaamsutra.data.Todo
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateOf
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import sh.calvin.reorderable.ReorderableItem

@Composable
fun TodoScreen(activeTodos: List<Todo>,
               onCheckedChange: (Boolean, Todo) -> Unit,
               onEditClicked: (Todo) -> Unit,
               onDeleteClicked: (Todo) -> Unit,
               modifier: Modifier
) {
    val lazyListState = rememberLazyListState()

    // single source of truth, ordered: not-done first, done after
    var todos by remember(activeTodos) {
        mutableStateOf(
            activeTodos.filter { !it.completed } + activeTodos.filter { it.completed }
        )
    }

    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        todos = todos.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    LazyColumn(state = lazyListState, modifier = modifier.fillMaxSize()) {
        items(todos, key = { it.id }) { todo ->
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
        item {
            Spacer(modifier = Modifier.height(75.dp))
        }
    }
}