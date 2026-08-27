package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Star


enum class Screen(val singular: String, val plural: String) {
//    HOME("Home"),
    HABITS("Habit", "Habits"),
    GOALS("Goal", "Goals"),
    TODO("ToDo", "Todo" )
}

@Composable
fun AppBottomBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
){
    NavigationBar{
//        NavigationBarItem(
//            selected = currentScreen == Screen.HOME,
//            onClick = {onScreenSelected(Screen.HOME)},
//            icon = {Icon(Icons.Default.Home, contentDescription = "Home")},
//            label = { Text("Home") }
//        )
        NavigationBarItem(
            selected = currentScreen == Screen.HABITS,
            onClick = {onScreenSelected(Screen.HABITS)},
            icon = {Icon(Icons.Default.CheckCircle, contentDescription = "Habits")},
            label = { Text("Habits") }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.GOALS,
            onClick = {onScreenSelected(Screen.GOALS)},
            icon = {Icon(Icons.Default.Star, contentDescription = "Goals")},
            label = { Text("Goals") }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.TODO,
            onClick = {onScreenSelected(Screen.TODO)},
            icon = {Icon(Icons.Default.Done, contentDescription = "TODO")},
            label = { Text("TODO") }
        )
    }
}