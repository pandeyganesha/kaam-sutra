package com.pandeyganesha.kaamsutra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.pandeyganesha.kaamsutra.ui.components.AddHabitDialog
import com.pandeyganesha.kaamsutra.ui.components.DeleteHabitDialog
import com.pandeyganesha.kaamsutra.ui.components.NetWorthCard
import com.pandeyganesha.kaamsutra.ui.components.HabitRow
import com.pandeyganesha.kaamsutra.ui.components.Screen
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import com.pandeyganesha.kaamsutra.data.HabitLog
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import com.pandeyganesha.kaamsutra.data.scheduleTestNotification
import com.pandeyganesha.kaamsutra.data.scheduleMissedHabitSettlement
import com.pandeyganesha.kaamsutra.ui.components.AppBottomBar
import com.pandeyganesha.kaamsutra.ui.components.HomeScreen
import com.pandeyganesha.kaamsutra.ui.components.HabitScreen


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
    val habitDao = db.habitDao()
    val habitLogDao = db.habitLogDao()
    val coroutineScope = rememberCoroutineScope()
    val activeHabits by habitDao.getActiveHabits().collectAsState(initial = emptyList())
    val existingHabitNames = remember(activeHabits) { activeHabits.map { it.name }.toSet() }
    var showDialog by remember { mutableStateOf(false) }
    var habitBeingEdited by remember { mutableStateOf<Habit?>(null) }
    var habitBeingDeleted by remember { mutableStateOf<Habit?>(null) }
    val netWorth by habitLogDao.getNetWorth().collectAsState(initial = 0)
    val today = remember { LocalDate.now().toString() }
    val allHabitLogsForToday  by habitLogDao.getLogsForDate(today).collectAsState(initial = emptyList())
    var currentScreen by remember {mutableStateOf(Screen.HOME)}


    Scaffold(
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
                netWorth = netWorth ?:0,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.HABITS -> HabitScreen(
                activeHabits = activeHabits,
                allHabitLogsForToday = allHabitLogsForToday,
                onCheckedChange = { checked, habit ->
                    coroutineScope.launch {
                        habitLogDao.upsertLog(
                            HabitLog(
                                habitId = habit.id,
                                date = today,
                                pointsAwarded = if (checked) habit.worthDelta else 0
                            )
                        )
                    }
                },
                onEditClicked = { habit -> habitBeingEdited = habit },
                onDeleteClicked = { habit -> habitBeingDeleted = habit},
                modifier = Modifier.padding(innerPadding)
            )
            Screen.GOALS -> {
                // placeholder for now
            }
            Screen.SETTINGS -> {
                // placeholder for now
            }
        }
    }
    habitBeingDeleted?.let { habit ->
        DeleteHabitDialog(
            habitName = habit.name,
            onDismiss = { habitBeingDeleted = null },
            onConfirm = {
                coroutineScope.launch {
                    habitDao.softDeleteHabit(habit.copy(isActive = false))
                    habitBeingDeleted = null
                }
            }
        )
    }
    habitBeingEdited?.let { habit ->
        AddHabitDialog(
            habitName = habit.name,
            worthDelta = habit.worthDelta.toString(),
            existingHabitNames = existingHabitNames - habit.name,
            onDismiss = {
                habitBeingEdited = null
            },
            onConfirm = { habitName, worthDelta ->
                coroutineScope.launch {
                    habitDao.updateHabit(habit.copy(name = habitName, worthDelta = worthDelta))
                    habitBeingEdited = null
                }
            }
        )

    }
    if (showDialog) {
        AddHabitDialog(
            existingHabitNames = existingHabitNames,
            onDismiss = { showDialog = false },
            onConfirm = { habitName, worthDelta ->
                coroutineScope.launch {
                    habitDao.insertHabit(Habit(name = habitName, worthDelta = worthDelta))
                }
                showDialog = false
            })
    }
}