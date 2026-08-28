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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.pandeyganesha.kaamsutra.data.Habit

enum class RepeatType(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
    CUSTOM("Custom")
}

@Composable
fun AddHabitDialog(
    habit: Habit? = null,
    existingHabitNames: Set<String>,
    currentScreen: Screen,
    onDismiss: () -> Unit,
    onConfirm: (habit: Habit) -> Unit,
) {
    val existingHabitNamesExcludingItself = existingHabitNames - habit?.name
    var repeatDays by remember { mutableStateOf(habit?.repeatDays?.toString() ?: "") }
    var habitNameText by remember { mutableStateOf(habit?.name ?: "") }
    val isDuplicate = habitNameText in existingHabitNamesExcludingItself
    val nameRequester = remember { FocusRequester() }
    val daysFocusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var selected by remember {
        mutableStateOf(habit?.repeatType ?: RepeatType.DAILY)
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
                    RepeatType.entries.forEach { repeatType ->
                        FilterChip(
                            selected = selected == repeatType,
                            onClick = {
                                selected = repeatType
                            },
                            label = {
                                Text(repeatType.displayName)
                            }
                        )
                    }

                }
                if (selected == RepeatType.CUSTOM) {
                    OutlinedTextField(
                        value = repeatDays,
                        onValueChange = { value ->
                            if (value.all { it.isDigit() }) {
                                repeatDays = value
                            }
                        },
                        label = { Text("No Of Days") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.focusRequester(daysFocusRequester)
                    )

                    LaunchedEffect(Unit) {
                        daysFocusRequester.requestFocus()
                        keyboard?.show()
                    }
                }
            }
        },
        confirmButton = {

            TextButton(onClick = {
                onConfirm(
                    habit?.copy(
                        name = habitNameText,
                        repeatType = selected,
                        repeatDays = repeatDays.toIntOrNull()
                    ) ?: Habit(
                        name = habitNameText,
                        repeatType = selected,
                        repeatDays = repeatDays.toIntOrNull()
                    )
                )
            },
                enabled = !isDuplicate && habitNameText.isNotBlank() && (selected != RepeatType.CUSTOM || repeatDays.isNotBlank())
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