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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text


@Composable
fun GoalScreen(activeGoals: List<Goal>,
               onCheckedChange: (Boolean, Goal) -> Unit,
               onEditClicked: (Goal) -> Unit,
               onDeleteClicked: (Goal) -> Unit,
               onSortOrderUpdate: (List<Goal>) -> Unit,
               modifier: Modifier
) {
    val lazyListState = rememberLazyListState()
    var goalsNotDone by remember(activeGoals) {
        mutableStateOf(activeGoals.filter { !it.completed })
    }
    val goalsDone = activeGoals.filter { it.completed }
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        goalsNotDone = goalsNotDone.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

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
            items(goalsDone, key = { it.id }) { goal ->
                GoalRow(
                    goal = goal,
                    isChecked = goal.completed,
                    onCheckedChange = { checked -> onCheckedChange(checked, goal) },
                    onEditClick = { onEditClicked(goal) },
                    onDeleteClick = { onDeleteClicked(goal) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(75.dp))
            }
        }
    }
}