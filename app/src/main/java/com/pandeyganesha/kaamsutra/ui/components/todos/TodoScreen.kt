package com.pandeyganesha.kaamsutra.ui.components.todos

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.pandeyganesha.kaamsutra.data.DatabaseProvider
import com.pandeyganesha.kaamsutra.data.Tag
import kotlinx.coroutines.launch


@Composable
fun TodoScreen(
    activeTodos: List<Todo>,
    tags: List<Tag> = emptyList(),
    todoTagsMap: Map<String, List<Tag>> = emptyMap(),
    onCheckedChange: (Boolean, Todo) -> Unit,
    onEditClicked: (Todo) -> Unit,
    onDeleteClicked: (Todo) -> Unit,
    onSortOrderUpdate: (List<Todo>) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context.applicationContext)
    val tagDao = db.tagDao()
    val coroutineScope = rememberCoroutineScope()


    val all = remember { Tag(id = "ALL_TAG_ID", name = "All") }
    val tagsWithAll = listOf(all) + tags
    var selected by remember {mutableStateOf( all)}
    var showTagInputField by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    val filteredTodos = remember(activeTodos, selected, todoTagsMap) {
        if (selected == all) activeTodos
        else activeTodos.filter { todo -> todoTagsMap[todo.id]?.contains(selected) == true }
    }

    var todosNotDone by remember(filteredTodos) {
        mutableStateOf(filteredTodos.filter { !it.completed })
    }
    val todosDone = filteredTodos.filter { it.completed }

    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        todosNotDone = todosNotDone.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }
    Column(modifier = modifier.fillMaxSize()) {
        val chipsScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top=14.dp)
                .nestedScroll(object: NestedScrollConnection {
                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset = available
                })
                .horizontalScroll(chipsScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tagsWithAll.forEach { tag ->
                FilterChip(
                    selected = selected == tag,
                    onClick = { selected = tag },
                    label = { Text(tag.name) }
                )
            }
            IconButton(
                onClick = { showTagInputField = true },
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add tag")
            }
        }
        LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {
            items(todosNotDone, key = { it.id }) { todo ->
                ReorderableItem(reorderableState, key = todo.id) { isDragging ->
                    TodoRow(
                        todo = todo,
                        todoTags = todoTagsMap[todo.id] ?: emptyList(),
                        showTags = selected == all,
                        isChecked = todo.completed,
                        onCheckedChange = { checked -> onCheckedChange(checked, todo) },
                        onEditClick = { onEditClicked(todo) },
                        onDeleteClick = { onDeleteClicked(todo) },
                        modifier = Modifier.longPressDraggableHandle(
                            onDragStopped = {
                                val size = todosNotDone.size
                                val reordered = todosNotDone.mapIndexed { index, t ->
                                    t.copy(sortOrder = size - 1 - index)
                                }
                                todosNotDone = reordered
                                onSortOrderUpdate(reordered)
                            }
                        )
                    )
                }
            }
            if (todosDone.isNotEmpty()) {
                item {
                    Text(
                        "Done",
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 28.dp,
                            bottom = 8.dp
                        )
                    )
                }
                items(todosDone, key = { it.id }) { todo ->
                    TodoRow(
                        todo = todo,
                        todoTags = todoTagsMap[todo.id] ?: emptyList(),
                        showTags = selected == all,
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
        if (showTagInputField) {
            EditTagDialog(
                tags = tags,
                onDismiss = { showTagInputField = false },
                onConfirm = { createdTags, deletedTags ->
                    coroutineScope.launch {
                        createdTags.forEach { tagDao.createTag(it) }
                        deletedTags.forEach { tagDao.deleteTag(it) }
                    }
                    showTagInputField = false
                }
            )
        }
    }
}