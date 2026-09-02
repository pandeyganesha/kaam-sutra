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
import androidx.compose.ui.unit.dp
import com.pandeyganesha.kaamsutra.Screen
import com.pandeyganesha.kaamsutra.data.Habit
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange

enum class RepeatType(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
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
    var habitNameField by remember {
        mutableStateOf(
            TextFieldValue(
                text = habit?.name ?: "",
                selection = TextRange(habit?.name?.length ?: 0)
            )
        )
    }
    val isDuplicate = habitNameField.text in existingHabitNamesExcludingItself
    val nameRequester = remember { FocusRequester() }
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
                    value = habitNameField,
                    onValueChange = { habitNameField = it },
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
            }
        },
        confirmButton = {

            TextButton(onClick = {
                onConfirm(
                    habit?.copy(
                        name = habitNameField.text,
                        repeatType = selected,
                        repeatDays = repeatDays.toIntOrNull()
                    ) ?: Habit(
                        name = habitNameField.text,
                        repeatType = selected,
                        repeatDays = repeatDays.toIntOrNull()
                    )
                )
            },
                enabled = !isDuplicate && habitNameField.text.isNotBlank()
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