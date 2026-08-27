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
fun AddTaskDialog(
    taskName: String = "",
    existingTaskNames: Set<String>,
    currentScreen: Screen,
    onDismiss: () -> Unit,
    onConfirm: (taskName: String) -> Unit,
) {
    var taskNameText by remember { mutableStateOf(taskName) }
    val isDuplicate = taskNameText in existingTaskNames

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${currentScreen.singular}")},
        text = {
            Column {
                OutlinedTextField(
                    value = taskNameText,
                    onValueChange = { taskNameText = it },
                    label = {Text("Enter ${currentScreen.singular}")}
                )
                if (isDuplicate) {
                    Text(
                        text = "${currentScreen.singular} already exists",
                        color = Color.Red
                    )
                }
            }
        },
        confirmButton = {

            TextButton(onClick = {
                onConfirm(taskNameText)
            },
                enabled = !isDuplicate && taskNameText.isNotBlank()
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