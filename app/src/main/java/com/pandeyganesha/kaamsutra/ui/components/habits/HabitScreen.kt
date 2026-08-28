package com.pandeyganesha.kaamsutra.ui.components.habits

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pandeyganesha.kaamsutra.data.Habit
import com.pandeyganesha.kaamsutra.data.HabitLog
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
import com.pandeyganesha.kaamsutra.ui.components.TaskRow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset


enum class RepeatTypeChip(val displayName: String) {
    ALL("All"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
    CUSTOM("Custom")
}

@Composable
fun HabitScreen(activeHabits: List<Habit>,
                allHabitLogsForCurrentPeriods: List<HabitLog>,
                onCheckedChange: (Boolean, Habit) -> Unit,
                onEditClicked: (Habit) -> Unit,
                onDeleteClicked: (Habit) -> Unit,
                onSortOrderUpdate: (List<Habit>) -> Unit,
                modifier: Modifier
                ) {
    var selected by remember {
        mutableStateOf(RepeatTypeChip.ALL)
    }
    val lazyListState = rememberLazyListState()
    val filteredHabitsForRepeatType = filterHabitsByRepeatType(activeHabits, selected)
    var habitsWithLogs by remember(filteredHabitsForRepeatType, allHabitLogsForCurrentPeriods) {
        mutableStateOf(filteredHabitsForRepeatType) }

    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        habitsWithLogs = habitsWithLogs.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }
    Column(modifier = modifier.fillMaxSize()) {
        val chipsScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top=14.dp)
                .nestedScroll(object: NestedScrollConnection{
                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset = available
                })
                .horizontalScroll(chipsScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RepeatTypeChip.entries.forEach { repeatTypeChip ->
                FilterChip(
                    selected = selected == repeatTypeChip,
                    onClick = { selected = repeatTypeChip },
                    label = { Text(repeatTypeChip.displayName) }
                )
            }
        }
        LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {
            items(habitsWithLogs, key = { it.id }) { habit ->
                ReorderableItem(reorderableState, key = habit.id) { isDragging ->
                    HabitRow(
                        habit = habit,
                        showRepeatTypeOrDays = selected == RepeatTypeChip.ALL || selected == RepeatTypeChip.CUSTOM ,
                        isChecked = allHabitLogsForCurrentPeriods.any { it.habitId == habit.id && it.completed },
                        onCheckedChange = { checked -> onCheckedChange(checked, habit) },
                        onEditClick = { onEditClicked(habit) },
                        onDeleteClick = { onDeleteClicked(habit) },
                        modifier = Modifier.longPressDraggableHandle(
                            onDragStopped = {
                                val size = habitsWithLogs.size
                                val reordered = habitsWithLogs.mapIndexed { index, h ->
                                    h.copy(sortOrder = size - 1 - index)
                                }
                                habitsWithLogs = reordered
                                onSortOrderUpdate(reordered)
                            }
                        )
                    )
                }
            }
        }
    }
}

private fun filterHabitsByRepeatType(
    habits: List<Habit>,
    repeatTypeChip: RepeatTypeChip
): List<Habit> {
    if (repeatTypeChip == RepeatTypeChip.ALL) {
        return habits
    }

    return habits.filter {
        it.repeatType.displayName.equals(
            repeatTypeChip.displayName,
            ignoreCase = true
        )
    }
}