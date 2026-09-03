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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import com.pandeyganesha.kaamsutra.MyApp
import com.pandeyganesha.kaamsutra.Screen
import com.pandeyganesha.kaamsutra.data.Status
import com.pandeyganesha.kaamsutra.periodStartDateFor
import com.pandeyganesha.kaamsutra.ui.components.DeleteTaskDialog
import com.pandeyganesha.kaamsutra.ui.components.EmptyState
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate


enum class RepeatTypeChip(val displayName: String) {
    ALL("All"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
}

@Composable
fun HabitScreen(modifier: Modifier) {
    val db = (LocalContext.current.applicationContext as MyApp).db
    val scope = rememberCoroutineScope()
    val coroutineScope = rememberCoroutineScope()
    val habitDao = db.habitDao()
    val habitLogDao = db.habitLogDao()
    val activeHabits by habitDao.getHabits(Status.ACTIVE).collectAsState(initial = emptyList())
    var selected by remember {
        mutableStateOf(RepeatTypeChip.ALL)
    }

    val today = LocalDate.now()
    val relevantPeriodDates = remember(today) {
        listOf(
            today.toString(),
            today.with(DayOfWeek.MONDAY).toString(),
            today.withDayOfMonth(1).toString(),
            today.withDayOfYear(1).toString()
        )
    }
    val allHabitLogsForCurrentPeriods by habitLogDao.getLogsForDates(relevantPeriodDates)
        .collectAsState(initial = emptyList())
    var habitBeingEdited by remember { mutableStateOf<Habit?>(null) }
    var habitBeingDeleted by remember { mutableStateOf<Habit?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val existingHabits =  activeHabits.map { it.name }.toSet()
    val lazyListState = rememberLazyListState()
    val filteredHabitsForRepeatType = filterHabitsByRepeatType(activeHabits, selected)
    var habitsWithLogs by remember(filteredHabitsForRepeatType, allHabitLogsForCurrentPeriods) {
        mutableStateOf(filteredHabitsForRepeatType) }

    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        habitsWithLogs = habitsWithLogs.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    val onCheckedChange: (Boolean, Habit) -> Unit = { checked, habit ->
        coroutineScope.launch {
            habitLogDao.upsertLog(
                HabitLog(
                    habitId = habit.id,
                    habitDate = periodStartDateFor(habit, LocalDate.now()).toString(),
                    completed = checked
                )
            )
        }
    }
    val onEditClicked: (Habit) -> Unit = { habit -> habitBeingEdited = habit }
    val onDeleteClicked: (Habit) -> Unit = { habit -> habitBeingDeleted = habit }
    val onSortOrderUpdate: (List<Habit>) -> Unit = { reorderedList ->
        scope.launch {
            db.habitDao().updateHabits(reorderedList)
        }
    }

    if (activeHabits.isEmpty()){
        EmptyState(pageName = Screen.TODO, onClick = { showDialog = true })
    }
    else {
        Column(modifier = modifier.fillMaxSize()) {
            val chipsScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp)
                    .nestedScroll(object : NestedScrollConnection {
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
                            showRepeatTypeOrDays = selected == RepeatTypeChip.ALL,
                            isChecked = allHabitLogsForCurrentPeriods.any {
                                it.habitId == habit.id && it.completed &&
                                        it.habitDate == periodStartDateFor(
                                    habit,
                                    LocalDate.now()
                                ).toString()
                            },
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
    habitBeingDeleted?.let { habit ->
        DeleteTaskDialog(
            taskName = habit.name,
            onDismiss = { habitBeingDeleted = null },
            onConfirm = {
                coroutineScope.launch {
                    habitDao.softDeleteHabit(habit.id)
                    habitBeingDeleted = null
                }
            }
        )
    }
    habitBeingEdited?.let { habit ->
        AddHabitDialog (
            habit = habit,
            existingHabitNames = existingHabits,
            currentScreen = Screen.HABITS,
            onDismiss = {
                habitBeingEdited = null
            },
            onConfirm = { habit ->
                coroutineScope.launch {
                    habitDao.updateHabit(habit.copy(name = habit.name.replaceFirstChar { it.uppercase() }))
                    habitBeingEdited = null
                }
            }
        )
    }
    if (showDialog){
        AddHabitDialog(
            existingHabitNames = existingHabits,
            currentScreen = Screen.HABITS,
            onDismiss = { showDialog = false },
            onConfirm = { habit ->
                coroutineScope.launch {
                    habitDao.createHabit(habit.copy(name = habit.name.replaceFirstChar { it.uppercase() }))
                }
                showDialog = false
            })
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