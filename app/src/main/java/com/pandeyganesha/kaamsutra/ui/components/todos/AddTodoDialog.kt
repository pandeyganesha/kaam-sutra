package com.pandeyganesha.kaamsutra.ui.components.todos

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
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.pandeyganesha.kaamsutra.data.Todo
import androidx.compose.ui.unit.dp
import com.pandeyganesha.kaamsutra.Screen
import com.pandeyganesha.kaamsutra.data.Tag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange


@Composable
fun AddTodoDialog(
    todo: Todo? = null,
    todoTags: List<Tag> = emptyList(),
    tags: List<Tag> = emptyList(),
    existingTodoNames: Set<String>,
    currentScreen: Screen,
    onDismiss: () -> Unit,
    onConfirm: (todo: Todo, selectedTags: List<Tag>) -> Unit,
) {

    var todoNameField by remember {
        mutableStateOf(
            TextFieldValue(
                text = todo?.name ?: "",
                selection = TextRange(todo?.name?.length ?: 0)
            )
        )
    }
    val isDuplicate = todoNameField.text in existingTodoNames - todo?.name
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var selectedTags by remember { mutableStateOf(todoTags) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${currentScreen.singular}")},
        text = {
            Column {
                OutlinedTextField(
                    value = todoNameField,
                    onValueChange = { todoNameField = it },
                    label = { Text("Enter ${currentScreen.singular}") },
                    modifier = Modifier.focusRequester(focusRequester)
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    keyboard?.show()
                }
                if (tags.isNotEmpty()) {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tags.forEach { tag ->
                            InputChip(
                                selected = tag in selectedTags,
                                onClick = {
                                    selectedTags = if (tag in selectedTags)
                                        selectedTags - tag
                                    else
                                        selectedTags + tag
                                },
                                label = { Text(tag.name) }
                            )
                        }
                    }
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
                    todo?.copy(name = todoNameField.text) ?: Todo(name = todoNameField.text),
                    selectedTags
                )
            },
                enabled = !isDuplicate && todoNameField.text.isNotBlank()
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