package com.pandeyganesha.kaamsutra.ui.components.goals

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import com.pandeyganesha.kaamsutra.data.Goal
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import sh.calvin.reorderable.ReorderableItem
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.pandeyganesha.kaamsutra.MyApp
import com.pandeyganesha.kaamsutra.Screen
import com.pandeyganesha.kaamsutra.data.Status
import com.pandeyganesha.kaamsutra.ui.components.CollapsibleSectionHeader
import com.pandeyganesha.kaamsutra.ui.components.DeleteTaskDialog
import com.pandeyganesha.kaamsutra.ui.components.EmptyState
import kotlinx.coroutines.launch


@Composable
fun GoalScreen(
    modifier: Modifier
) {
    val db = (LocalContext.current.applicationContext as MyApp).db
    val coroutineScope = rememberCoroutineScope()
    val scope = rememberCoroutineScope()
    val goalDao = db.goalDao()
    val activeGoals by goalDao.getGoals(Status.ACTIVE).collectAsState(initial = emptyList())
    var goalBeingEdited by remember { mutableStateOf<Goal?>(null) }
    var goalBeingDeleted by remember { mutableStateOf<Goal?>(null) }
    val existingGoals = activeGoals.map { it.name }.toSet()
    var showDialog by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    var goalsNotDone by remember(activeGoals) {
        mutableStateOf(activeGoals.filter { !it.completed })
    }
    val goalsDone = activeGoals.filter { it.completed }
    var doneExpanded by remember { mutableStateOf(false) }
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        goalsNotDone = goalsNotDone.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }
    val onCheckedChange: (Boolean, Goal) -> Unit = { checked, goal ->
        coroutineScope.launch {
            goalDao.updateGoal(goal.copy(completed = checked))
        }
    }
    val onEditClicked: (Goal) -> Unit = { goal -> goalBeingEdited = goal }
    val onDeleteClicked: (Goal) -> Unit = { goal -> goalBeingDeleted = goal }
    val onSortOrderUpdate: (List<Goal>) -> Unit = { reorderedList ->
        scope.launch {
            db.goalDao().updateGoals(reorderedList)
        }
    }



    if (activeGoals.isEmpty()){
        EmptyState(pageName = Screen.TODO, onClick = { showDialog = true })

    }
    else {
        LazyColumn(state = lazyListState, modifier = modifier.fillMaxSize()) {
            items(goalsNotDone, key = { it.id }) { goal ->
                ReorderableItem(reorderableState, key = goal.id) { isDragging ->

                    GoalRow(
                        goal = goal,
                        isChecked = goal.completed,
                        onCheckedChange = { checked -> onCheckedChange(checked, goal) },
                        onEditClick = { onEditClicked(goal) },
                        onDeleteClick = { onDeleteClicked(goal) },
                        modifier = Modifier.longPressDraggableHandle(
                            onDragStopped = {
                                val size = goalsNotDone.size
                                val reordered = goalsNotDone.mapIndexed { index, t ->
                                    t.copy(sortOrder = size - 1 - index)
                                }
                                goalsNotDone = reordered
                                onSortOrderUpdate(reordered)
                            }
                        )
                    )
                }
            }
            if (goalsDone.isNotEmpty()) {
                item {
                    CollapsibleSectionHeader(
                        title = "Done",
                        expanded = doneExpanded,
                        onToggle = { doneExpanded = !doneExpanded }
                    )
                }
                if (doneExpanded) {
                    items(goalsDone, key = { it.id }) { goal ->
                        GoalRow(
                            goal = goal,
                            isChecked = goal.completed,
                            onCheckedChange = { checked -> onCheckedChange(checked, goal) },
                            onEditClick = { onEditClicked(goal) },
                            onDeleteClick = { onDeleteClicked(goal) }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(75.dp))
                }
            }
        }
    }
    goalBeingDeleted?.let { goal ->
        DeleteTaskDialog(
            taskName = goal.name,
            onDismiss = { goalBeingDeleted = null },
            onConfirm = {
                coroutineScope.launch {
                    goalDao.softDeleteGoal(goal.id)
                    goalBeingDeleted = null
                }
            }
        )
    }
    goalBeingEdited?.let { goal ->
        AddGoalDialog(
            goal = goal,
            existingGoalNames = existingGoals,
            onDismiss = {
                goalBeingEdited = null
            },
            onConfirm = { goalName ->
                coroutineScope.launch {
                    goalDao.updateGoal(goal.copy(name = goal.name.replaceFirstChar { it.uppercase() }))
                    goalBeingEdited = null
                }
            }
        )
    }

    if (showDialog){
        AddGoalDialog(
            existingGoalNames = existingGoals,
            onDismiss = { showDialog = false },
            onConfirm = { goal ->
                coroutineScope.launch {
                    goalDao.createGoal(goal.copy(name = goal.name.replaceFirstChar { it.uppercase() }))
                }
                showDialog = false
            })
    }
}