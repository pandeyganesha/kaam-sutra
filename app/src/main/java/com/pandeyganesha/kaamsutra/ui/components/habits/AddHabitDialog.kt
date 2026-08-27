package com.pandeyganesha.kaamsutra.ui.components.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.pandeyganesha.kaamsutra.ui.components.Screen
import androidx.compose.ui.unit.dp

enum class Option(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
    CUSTOM("Custom")
}

@Composable
fun AddHabitDialog(
    habitName: String = "",
    existingHabitNames: Set<String>,
    currentScreen: Screen,
    onDismiss: () -> Unit,
    onConfirm: (habitName: String) -> Unit,
) {
    var noOfDays by remember { mutableStateOf("1") }
    var habitNameText by remember { mutableStateOf(habitName) }
    val isDuplicate = habitNameText in existingHabitNames
    val nameRequester = remember { FocusRequester() }
    val daysFocusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var selected by remember {
        mutableStateOf(Option.DAILY)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${currentScreen.singular}")},
        text = {
            Column {
                OutlinedTextField(
                    value = habitNameText,
                    onValueChange = { habitNameText = it },
                    label = {Text("Enter ${currentScreen.singular}")},
                    modifier = Modifier.focusRequester(nameRequester)
                )
                LaunchedEffect(Unit) {
                    nameRequester.requestFocus()
                    keyboard?.show()
                }
                if (isDuplicate) {
                    Text(
                        text = "${currentScreen.singular} already exists",
                        color = Color.Red
                    )
                }
                Text("Repeat", Modifier.padding(top = 15.dp, bottom = 7.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Option.entries.forEach { option ->
                        FilterChip(
                            selected = selected == option,
                            onClick = {
                                selected = option
                            },
                            label = {
                                Text(option.displayName)
                            }
                        )
                    }

                }
                if (selected == Option.CUSTOM){
                    OutlinedTextField(
                        value = noOfDays,
                        onValueChange = { noOfDays = it },
                        label = {Text("No Of Days")},
                        modifier = Modifier.focusRequester(daysFocusRequester)
                    )
                }
                LaunchedEffect(selected) {
                    daysFocusRequester.requestFocus()
                    keyboard?.show()
                }
            }
        },
        confirmButton = {

            TextButton(onClick = {
                onConfirm(habitNameText)
            },
                enabled = !isDuplicate && habitNameText.isNotBlank() && (selected==Option.CUSTOM && noOfDays.isNotBlank())
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}