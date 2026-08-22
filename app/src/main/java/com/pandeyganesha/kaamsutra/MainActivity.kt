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
import com.pandeyganesha.kaamsutra.data.Task
import kotlinx.coroutines.launch
import com.pandeyganesha.kaamsutra.ui.components.AddTaskDialog
import com.pandeyganesha.kaamsutra.ui.components.DeleteTaskDialog
import com.pandeyganesha.kaamsutra.ui.components.Screen
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import com.pandeyganesha.kaamsutra.data.TaskLog
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
import com.pandeyganesha.kaamsutra.ui.components.AppBottomBar
import com.pandeyganesha.kaamsutra.ui.components.AppTopBar
import com.pandeyganesha.kaamsutra.ui.components.HomeScreen
import com.pandeyganesha.kaamsutra.ui.components.HabitScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Add
import com.pandeyganesha.kaamsutra.ui.components.GoalScreen
import com.pandeyganesha.kaamsutra.ui.components.TodoScreen


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
            intent.getSerializableExtra("notif_screen", Screen::class.java) ?: Screen.HOME
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("notif_screen") as? Screen ?: Screen.HOME
        }
        setContent {
            KaamSutraTheme {
                KaamSutraApp(screenToOpen)
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KaamSutraApp(screenToOpen: Screen) {

    val pagerState = rememberPagerState(
        initialPage = Screen.HOME.ordinal,
        pageCount = { Screen.entries.size}
    )
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context.applicationContext)
    val taskDao = db.taskDao()
    val taskLogDao = db.taskLogDao()
    val coroutineScope = rememberCoroutineScope()
    val currentScreen = Screen.entries[pagerState.currentPage]
    val activeHabits by taskDao.getActiveTasks(Screen.HABITS).collectAsState(initial = emptyList())
    val activeGoals by taskDao.getActiveTasks(Screen.GOALS).collectAsState(initial = emptyList())
    val activeTodos by taskDao.getActiveTasks(Screen.TODO).collectAsState(initial = emptyList())
    val existingNames = remember(activeHabits, activeGoals, activeTodos) {
        mapOf(
            Screen.HABITS to activeHabits.map { it.name }.toSet(),
            Screen.GOALS to activeGoals.map { it.name }.toSet(),
            Screen.TODO to activeTodos.map { it.name }.toSet()
        )
    }
    var showDialog by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf<Task?>(null) }
    var taskBeingDeleted by remember { mutableStateOf<Task?>(null) }
    val today = remember { LocalDate.now().toString() }
    val allHabitLogsForToday  by taskLogDao.getLogsForDate(today).collectAsState(initial = emptyList())
    val allGoalLogs by taskLogDao.getAllLogsFor(currentScreen).collectAsState(initial = emptyList())


    Scaffold(
        floatingActionButton = {
            if (currentScreen != Screen.HOME) {
                FloatingActionButton(
                    onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        },
        topBar = {
            AppTopBar(
                title = currentScreen.title,
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            AppBottomBar(
                currentScreen = currentScreen,
                onScreenSelected = { scope.launch {
                    pagerState.animateScrollToPage(
                        it.ordinal
                    )
                }},
            )
        },
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
        when (Screen.entries[page]) {
            Screen.HOME -> HomeScreen(
                modifier = Modifier.padding(innerPadding)
            )

            Screen.HABITS -> HabitScreen(
                activeHabits = activeHabits,
                allHabitLogsForToday = allHabitLogsForToday,
                onCheckedChange = { checked, habit ->
                    coroutineScope.launch {
                        taskLogDao.upsertLog(
                            TaskLog(
                                taskId = habit.id,
                                date = today,
                                completed = checked
                            )
                        )
                    }
                },
                onEditClicked = { habit -> taskBeingEdited = habit },
                onDeleteClicked = { habit -> taskBeingDeleted = habit },
                modifier = Modifier.padding(innerPadding)
            )

            Screen.GOALS -> GoalScreen(
                activeGoals = activeGoals,
                allGoalLogs = allGoalLogs,
                onCheckedChange = { checked, goal ->
                    coroutineScope.launch {
                        taskLogDao.upsertLog(
                            TaskLog(
                                taskId = goal.id,
                                date = today,
                                completed = checked
                            )
                        )
                    }
                },
                onEditClicked = { goal -> taskBeingEdited = goal },
                onDeleteClicked = { goal -> taskBeingDeleted = goal },
                modifier = Modifier.padding(innerPadding)
            )

            Screen.TODO -> TodoScreen(
                activeTodos = activeTodos,
                allTodoLogs = allGoalLogs,
                onCheckedChange = { checked, todo ->
                    coroutineScope.launch {
                        taskLogDao.upsertLog(
                            TaskLog(
                                taskId = todo.id,
                                date = today,
                                completed = checked
                            )
                        )
                    }
                },
                onEditClicked = { todo -> taskBeingEdited = todo },
                onDeleteClicked = { todo -> taskBeingDeleted = todo },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
    }
    taskBeingDeleted?.let { task ->
        DeleteTaskDialog(
            taskName = task.name,
            onDismiss = { taskBeingDeleted = null },
            onConfirm = {
                coroutineScope.launch {
                    taskDao.softDeleteTask(task.id)
                    taskBeingDeleted = null
                }
            }
        )
    }
    taskBeingEdited?.let { task ->
        AddTaskDialog(
            taskName = task.name,
            existingTaskNames = existingNames[currentScreen].orEmpty() - task.name,
            currentScreen = currentScreen,
            onDismiss = {
                taskBeingEdited = null
            },
            onConfirm = { taskName ->
                coroutineScope.launch {
                    taskDao.updateTask(task.copy(name = taskName))
                    taskBeingEdited = null
                }
            }
        )

    }
    if (showDialog) {
        AddTaskDialog(
            existingTaskNames = existingNames[currentScreen].orEmpty(),
            currentScreen = currentScreen,
            onDismiss = { showDialog = false },
            onConfirm = { taskName ->
                coroutineScope.launch {
                    taskDao.insertTask(Task(name = taskName, taskType = currentScreen))
                }
                showDialog = false
            })
    }
}