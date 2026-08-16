package com.pandeyganesha.kaamsutra.ui.components

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



@Composable
fun AddHabitDialog(
    habitName: String = "",
    worthDelta: String = "",
    existingHabitNames: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (habitName: String, worthDelta: Int) -> Unit,
) {
    var habitNameText by remember { mutableStateOf(habitName) }
    var worthDeltaText by remember { mutableStateOf(worthDelta) }
    val isDuplicate = habitNameText in existingHabitNames
    val isZero = worthDeltaText.toIntOrNull() == 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Habit")},
        text = {
            Column {
                OutlinedTextField(
                    value = habitNameText,
                    onValueChange = { habitNameText = it },
                    label = {Text("Habit Name")}
                )
                if (isDuplicate) {
                    Text(
                        text = "Habit name already exists",
                        color = Color.Red
                    )
                }
                OutlinedTextField(
                    value = worthDeltaText,
                    onValueChange = { worthDeltaText = it },
                    label = { Text("Worth") }
                )
                if (isZero){
                    Text(
                        text = "Zero? That's not worth tracking",
                        color = Color.Red
                    )
                }
            }
        },
        confirmButton = {

            TextButton(onClick = {
                onConfirm(habitNameText, worthDeltaText.toIntOrNull() ?: 0)
            },
                enabled = !isDuplicate && !isZero && habitNameText.isNotBlank() && worthDeltaText.isNotBlank()
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