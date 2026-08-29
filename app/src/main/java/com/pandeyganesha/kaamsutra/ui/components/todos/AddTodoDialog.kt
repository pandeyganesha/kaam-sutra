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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.pandeyganesha.kaamsutra.data.Todo
import com.pandeyganesha.kaamsutra.ui.components.Screen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.unit.dp


@Composable
fun AddTodoDialog(
    todo: Todo? = null,
    existingTodoNames: Set<String>,
    currentScreen: Screen,
    onDismiss: () -> Unit,
    onConfirm: (todo: Todo) -> Unit,
) {
    var todoNameText by remember { mutableStateOf(todo?.name ?: "") }
    val isDuplicate = todoNameText in existingTodoNames
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var newTag by remember { mutableStateOf("") }
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
                Text("Tags")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = { /* e.g. select existing tag */ },
                            label = { Text(tag) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove tag",
                                    modifier = Modifier
                                        .size(InputChipDefaults.AvatarSize)
                                        .clickable { /* remove this tag */ }
                                )
                            }
                        )
                }
            }
                OutlinedTextField(
                    value = newTag,
                    onValueChange = {newTag = it},
                    label = {Text("Tag")},
                    trailingIcon = {
                        IconButton(onClick = {
                            tags.add(newTag)
                            newTag = ""
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Add tag")
                        }
                    },
                    modifier = Modifier.focusRequester(focusRequester)
                )

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