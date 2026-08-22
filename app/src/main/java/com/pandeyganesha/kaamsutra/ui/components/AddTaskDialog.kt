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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp



@Composable
fun AddTaskDialog(
    taskName: String = "",
    taskPoints: Int = 0,
    existingTaskNames: Set<String>,
    currentScreen: Screen,
    onDismiss: () -> Unit,
    onConfirm: (taskName: String, points: Int) -> Unit,
) {
    var taskNameText by remember { mutableStateOf(taskName) }
    var taskPointsText by remember { mutableStateOf(if (taskPoints == 0) "" else taskPoints.toString()) }
    val isDuplicate = taskNameText in existingTaskNames
    val parsedPoints = taskPointsText.toIntOrNull()
    val isPointsValid = parsedPoints != null && parsedPoints >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${currentScreen.title}")},
        text = {
            Column {
                OutlinedTextField(
                    value = taskNameText,
                    onValueChange = { taskNameText = it },
                    label = {Text("${currentScreen.title} Name")}
                )
                if (isDuplicate) {
                    Text(
                        text = "${currentScreen.title} already exists",
                        color = Color.Red
                    )
                }
                Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                OutlinedTextField(
                    value = taskPointsText,
                    onValueChange = { taskPointsText = it },
                    label = { Text("Points") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (taskPointsText.isNotBlank() && !isPointsValid) {
                    Text(
                        text = "Enter a valid non-negative number",
                        color = Color.Red
                    )
                }
            }
        },
        confirmButton = {

            TextButton(onClick = {
                onConfirm(taskNameText, parsedPoints ?: 0)
            },
                enabled = !isDuplicate && taskNameText.isNotBlank() && isPointsValid
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