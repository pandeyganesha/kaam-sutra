package com.pandeyganesha.kaamsutra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.pandeyganesha.kaamsutra.ui.theme.KaamSutraTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.collections.emptyList
import androidx.compose.runtime.collectAsState
import com.pandeyganesha.kaamsutra.data.DatabaseProvider
import com.pandeyganesha.kaamsutra.data.Habit
import kotlinx.coroutines.launch
import com.pandeyganesha.kaamsutra.ui.components.DeleteTaskDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import com.pandeyganesha.kaamsutra.data.HabitLog
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FloatingActionButton
import com.pandeyganesha.kaamsutra.data.scheduleTestNotification
import com.pandeyganesha.kaamsutra.data.scheduleMissedHabitSettlement
import com.pandeyganesha.kaamsutra.ui.components.AppTopBar
import com.pandeyganesha.kaamsutra.ui.components.habits.HabitScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Add
import com.pandeyganesha.kaamsutra.data.Goal
import com.pandeyganesha.kaamsutra.data.Status
import com.pandeyganesha.kaamsutra.data.Tag
import com.pandeyganesha.kaamsutra.data.Todo
import com.pandeyganesha.kaamsutra.data.TodoTag
import com.pandeyganesha.kaamsutra.ui.components.goals.GoalScreen
import com.pandeyganesha.kaamsutra.ui.components.todos.TodoScreen
import com.pandeyganesha.kaamsutra.ui.components.EmptyState
import com.pandeyganesha.kaamsutra.ui.components.goals.AddGoalDialog
import com.pandeyganesha.kaamsutra.ui.components.habits.AddHabitDialog
import com.pandeyganesha.kaamsutra.ui.components.habits.RepeatType
import com.pandeyganesha.kaamsutra.ui.components.todos.AddTodoDialog
import java.time.DayOfWeek


class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // optional: handle the user's response here, if you want
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        scheduleTestNotification(applicationContext)
        scheduleMissedHabitSettlement(applicationContext)
        enableEdgeToEdge()
        val screenToOpen: Screen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("notif_screen", Screen::class.java) ?: Screen.HABITS
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("notif_screen") as? Screen ?: Screen.HABITS
        }
        setContent {
            KaamSutraTheme {
                KaamSutraApp(screenToOpen)
            }
        }
    }
}

enum class Screen(val singular: String, val plural: String) {
    //    HOME("Home"),
    HABITS("Habit", "Habits"),
    GOALS("Goal", "Goals"),
    TODO("ToDo", "Todo" )
}

fun periodStartDateFor(habit: Habit, date: LocalDate): LocalDate {
    return when (habit.repeatType) {
        RepeatType.DAILY -> date
        RepeatType.WEEKLY -> date.with(DayOfWeek.MONDAY)
        RepeatType.MONTHLY -> date.withDayOfMonth(1)
        RepeatType.YEARLY -> date.withDayOfYear(1)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KaamSutraApp(screenToOpen: Screen) {

    val pagerState = rememberPagerState(
        initialPage = Screen.GOALS.ordinal,
        pageCount = { Screen.entries.size}
    )
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context.applicationContext)
    val todoDao = db.todoDao()
    val habitDao = db.habitDao()
    val goalDao = db.goalDao()
    val habitLogDao = db.habitLogDao()
    val tagDao = db.tagDao()
    val todoTagDao = db.todoTagDao()
    val coroutineScope = rememberCoroutineScope()
    val currentScreen = Screen.entries[pagerState.currentPage]
    val activeHabits by habitDao.getHabits(Status.ACTIVE).collectAsState(initial = emptyList())
    val activeGoals by goalDao.getGoals(Status.ACTIVE).collectAsState(initial = emptyList())
    val activeTodos by todoDao.getTodos(Status.ACTIVE).collectAsState(initial = emptyList())
    val allTags by tagDao.getTags().collectAsState(initial = emptyList())
    val allTodoTagRows by todoTagDao.getAllTodoTags().collectAsState(initial = emptyList())
    val todoTagsMap = remember(allTodoTagRows) {
        allTodoTagRows.groupBy({ it.todoId }, { it.tag })
    }

    val isEmptyMap = mapOf(
        Screen.HABITS to activeHabits.isEmpty(),
        Screen.GOALS to activeGoals.isEmpty(),
        Screen.TODO to activeTodos.isEmpty()
    )
    val existingNames = remember(activeHabits, activeGoals, activeTodos) {
        mapOf(
            Screen.HABITS to activeHabits.map { it.name }.toSet(),
            Screen.GOALS to activeGoals.map { it.name }.toSet(),
            Screen.TODO to activeTodos.map { it.name }.toSet()
        )
    }
    var showDialog by remember { mutableStateOf(false) }
    var todoBeingEdited by remember { mutableStateOf<Todo?>(null) }
    var todoBeingDeleted by remember { mutableStateOf<Todo?>(null) }
    var goalBeingEdited by remember { mutableStateOf<Goal?>(null) }
    var goalBeingDeleted by remember { mutableStateOf<Goal?>(null) }
    var habitBeingEdited by remember { mutableStateOf<Habit?>(null) }
    var habitBeingDeleted by remember { mutableStateOf<Habit?>(null) }
    var todoTagsBeingEdited by remember { mutableStateOf<List<Tag>>(emptyList()) }
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


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }

        },
        topBar = {
            AppTopBar(
                title = currentScreen.plural,
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
        ) { page ->
            if (isEmptyMap[currentScreen] ?: false) {
                EmptyState(pageName = currentScreen, onClick = { showDialog = true })
            }
            else {
                when (Screen.entries[page]) {
                    Screen.HABITS -> {
                        HabitScreen(
                            activeHabits = activeHabits,
                            allHabitLogsForCurrentPeriods = allHabitLogsForCurrentPeriods,
                            onCheckedChange = { checked, habit ->
                                coroutineScope.launch {
                                    habitLogDao.upsertLog(
                                        HabitLog(
                                            habitId = habit.id,
                                            habitDate = periodStartDateFor(habit, LocalDate.now()).toString(),
                                            completed = checked
                                        )
                                    )
                                }
                            },
                            onEditClicked = { habit -> habitBeingEdited = habit },
                            onDeleteClicked = { habit -> habitBeingDeleted = habit },
                            onSortOrderUpdate = { reorderedList ->
                                scope.launch {
                                    db.habitDao().updateHabits(reorderedList)
                                }
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    Screen.GOALS -> GoalScreen(
                        activeGoals = activeGoals,
                        onCheckedChange = { checked, goal ->
                            coroutineScope.launch {
                                goalDao.updateGoal(goal.copy(completed = checked))
                            }
                        },
                        onEditClicked = { goal -> goalBeingEdited = goal },
                        onDeleteClicked = { goal -> goalBeingDeleted = goal },
                        onSortOrderUpdate = { reorderedList ->
                            scope.launch {
                                db.goalDao().updateGoals(reorderedList)
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )

                    Screen.TODO -> TodoScreen(
                        activeTodos = activeTodos,
                        tags = allTags,
                        todoTagsMap = todoTagsMap,
                        onCheckedChange = { checked, todo ->
                            coroutineScope.launch {
                                todoDao.updateTodo(todo.copy(completed = checked))
                            }
                        },
                        onEditClicked = { todo ->
                            coroutineScope.launch {
                                todoTagsBeingEdited = todoTagDao.getTagsForTodo(todo.id)
                                todoBeingEdited = todo
                            }
                        },
                        onDeleteClicked = { todo -> todoBeingDeleted = todo },
                        onSortOrderUpdate = { reorderedList ->
                            scope.launch {
                                db.todoDao().updateTodos(reorderedList)
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
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
            existingHabitNames = existingNames[currentScreen].orEmpty(),
            currentScreen = currentScreen,
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
    todoBeingDeleted?.let { todo ->
        DeleteTaskDialog(
            taskName = todo.name,
            onDismiss = { todoBeingDeleted = null },
            onConfirm = {
                coroutineScope.launch {
                    todoDao.softDeleteTodo(todo.id)
                    todoBeingDeleted = null
                }
            }
        )
    }
    todoBeingEdited?.let { todo ->
        AddTodoDialog(
            todo = todo,
            tags = allTags,
            todoTags = todoTagsBeingEdited,
            existingTodoNames = existingNames[currentScreen].orEmpty(),
            currentScreen = currentScreen,
            onDismiss = {
                todoBeingEdited = null
            },
            onConfirm = { updatedTodo, selectedTags ->
                coroutineScope.launch {
                    todoDao.updateTodo(updatedTodo.copy(name = updatedTodo.name.replaceFirstChar { it.uppercase() }))
                    todoTagDao.deleteAllForTodo(updatedTodo.id)
                    selectedTags.forEach { tag ->
                        todoTagDao.insert(TodoTag(todoId = updatedTodo.id, tagId = tag.id))
                    }
                    todoBeingEdited = null
                }
            }
        )
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
            existingGoalNames = existingNames[currentScreen].orEmpty(),
            currentScreen = currentScreen,
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
    if (showDialog) {
        when (currentScreen) {
            Screen.HABITS ->
                AddHabitDialog(
                    existingHabitNames = existingNames[currentScreen].orEmpty(),
                    currentScreen = currentScreen,
                    onDismiss = { showDialog = false },
                    onConfirm = { habit ->
                        coroutineScope.launch {
                            habitDao.createHabit(habit.copy(name = habit.name.replaceFirstChar { it.uppercase() }))
                        }
                        showDialog = false
                    })

            Screen.GOALS ->
                AddGoalDialog(
                    existingGoalNames = existingNames[currentScreen].orEmpty(),
                    currentScreen = currentScreen,
                    onDismiss = { showDialog = false },
                    onConfirm = { goal ->
                        coroutineScope.launch {
                            goalDao.createGoal(goal.copy(name = goal.name.replaceFirstChar { it.uppercase() }))
                        }
                        showDialog = false
                    })

            Screen.TODO ->
                AddTodoDialog(
                    tags = allTags,
                    existingTodoNames = existingNames[currentScreen].orEmpty(),
                    currentScreen = currentScreen,
                    onDismiss = { showDialog = false },
                    onConfirm = { todo, selectedTags ->
                        coroutineScope.launch {
                            todoDao.createTodo(todo.copy(name = todo.name.replaceFirstChar { it.uppercase() }))
                            selectedTags.forEach { tag ->
                                todoTagDao.insert(TodoTag(todoId = todo.id, tagId = tag.id))
                            }
                        }
                        showDialog = false
                    }
                )
        }
    }
}