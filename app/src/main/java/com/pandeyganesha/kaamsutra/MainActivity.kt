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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
        setContent {
            KaamSutraTheme {
                KaamSutraApp()
            }
        }
    }
}



@Composable
fun KaamSutraApp() {

    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context.applicationContext)
    val taskDao = db.taskDao()
    val taskLogDao = db.taskLogDao()
    val coroutineScope = rememberCoroutineScope()
    var currentScreen by remember {mutableStateOf(Screen.HOME)}
    val activeTasks by taskDao.getActiveTasks(currentScreen).collectAsState(initial = emptyList())
    val existingTaskNames = remember(activeTasks) { activeTasks.map { it.name }.toSet() }
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
                onScreenSelected = { currentScreen = it },
                onAddClick = { showDialog = true }
            )
        },
    ) { innerPadding ->
        when (currentScreen) {
            Screen.HOME -> HomeScreen(
                modifier = Modifier.padding(innerPadding)
            )
            Screen.HABITS -> HabitScreen(
                activeHabits = activeTasks,
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
                onDeleteClicked = { habit -> taskBeingDeleted = habit},
                modifier = Modifier.padding(innerPadding)
            )
            Screen.GOALS -> GoalScreen(
                activeGoals = activeTasks,
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
                onEditClicked = { habit -> taskBeingEdited = habit },
                onDeleteClicked = { habit -> taskBeingDeleted = habit},
                modifier = Modifier.padding(innerPadding)
            )
            Screen.TODO -> {
                // placeholder for now
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
            existingTaskNames = existingTaskNames - task.name,
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
            existingTaskNames = existingTaskNames,
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