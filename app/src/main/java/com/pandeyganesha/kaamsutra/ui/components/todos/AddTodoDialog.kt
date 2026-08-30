package com.pandeyganesha.kaamsutra.ui.components.todos

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.pandeyganesha.kaamsutra.data.Todo
import com.pandeyganesha.kaamsutra.ui.components.Screen
import androidx.compose.runtime.mutableStateListOf
import com.pandeyganesha.kaamsutra.data.Tag


@Composable
fun AddTodoDialog(
    todo: Todo? = null,
    tags: List<Tag> = emptyList(),
    existingTodoNames: Set<String>,
    currentScreen: Screen,
    onDismiss: () -> Unit,
    onConfirm: (todo: Todo) -> Unit,
) {

    var todoNameText by remember { mutableStateOf(todo?.name ?: "") }
    val isDuplicate = todoNameText in existingTodoNames - todo?.name
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val tags = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${currentScreen.singular}")},
        text = {
            Column {
                OutlinedTextField(
                    value = todoNameText,
                    onValueChange = { todoNameText = it },
                    label = { Text("Enter ${currentScreen.singular}") },
                    modifier = Modifier.focusRequester(focusRequester)
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    keyboard?.show()
                }
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
                onConfirm(
                    todo?.copy(
                        name = todoNameText
                    ) ?: Todo(
                        name = todoNameText
                    )
                )
            },
                enabled = !isDuplicate && todoNameText.isNotBlank()
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