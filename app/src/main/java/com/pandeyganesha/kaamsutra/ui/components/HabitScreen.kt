package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pandeyganesha.kaamsutra.data.Habit
import com.pandeyganesha.kaamsutra.data.HabitLog
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import sh.calvin.reorderable.ReorderableItem
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text

@Composable
fun HabitScreen(activeHabits: List<Habit>,
                allHabitLogsForToday: List<HabitLog>,
                onCheckedChange: (Boolean, Habit) -> Unit,
                onEditClicked: (Habit) -> Unit,
                onDeleteClicked: (Habit) -> Unit,
                onSortOrderUpdate: (List<Habit>) -> Unit,
                modifier: Modifier
                ) {

    val lazyListState = rememberLazyListState()
    var habitsNotDone by remember(activeHabits, allHabitLogsForToday) {
        mutableStateOf(activeHabits.filter { habit ->
            allHabitLogsForToday.none {
                it.habitId == habit.id && it.completed
            }
        })
    }
    val habitsDone = activeHabits.filter { habit -> allHabitLogsForToday.any {
        it.habitId == habit.id && it.completed
    } }
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        habitsNotDone = habitsNotDone.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }
    LazyColumn(state = lazyListState, modifier = modifier.fillMaxSize()) {
        items(habitsNotDone, key = {it.id}) { habit ->
            ReorderableItem(reorderableState, key = habit.id) { isDragging ->
            TaskRow(
                taskName = habit.name,
                isChecked = allHabitLogsForToday.any { it.habitId == habit.id && it.completed},
                onCheckedChange = { checked -> onCheckedChange(checked, habit) },
                onEditClick = { onEditClicked(habit) },
                onDeleteClick = { onDeleteClicked(habit) },
                modifier = Modifier.longPressDraggableHandle(
                    onDragStopped = {
                        val size = habitsNotDone.size
                        val reordered = habitsNotDone.mapIndexed { index, h ->
                            h.copy(sortOrder = size - 1 - index)
                        }
                        habitsNotDone = reordered
                        onSortOrderUpdate(reordered)
                    }
                )
            )
            }
        }
        if (habitsDone.isNotEmpty()) {
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
            items(habitsDone, key = { it.id }) { habit ->
                TaskRow(
                    taskName = habit.name,
                    isChecked = allHabitLogsForToday.any { it.habitId == habit.id && it.completed },
                    onCheckedChange = { checked -> onCheckedChange(checked, habit) },
                    onEditClick = { onEditClicked(habit) },
                    onDeleteClick = { onDeleteClicked(habit) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(75.dp))
            }
        }
    }
}