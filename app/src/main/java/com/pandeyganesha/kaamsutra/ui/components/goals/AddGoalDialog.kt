package com.pandeyganesha.kaamsutra.ui.components.goals

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
import com.pandeyganesha.kaamsutra.data.Goal
import com.pandeyganesha.kaamsutra.ui.components.utils.DatePickerField
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import com.pandeyganesha.kaamsutra.Screen
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange

@Composable
fun AddGoalDialog(
    goal: Goal? = null,
    existingGoalNames: Set<String>,
    currentScreen: Screen,
    onDismiss: () -> Unit,
    onConfirm: (goal: Goal) -> Unit,
) {
    val existingGoalNamesExcludingItself = existingGoalNames - goal?.name
    var goalNameField by remember {
        mutableStateOf(
            TextFieldValue(
                text = goal?.name ?: "",
                selection = TextRange(goal?.name?.length ?: 0)
            )
        )
    }
    val isDuplicate = goalNameField.text in existingGoalNamesExcludingItself
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var pickedDate by remember { mutableStateOf(goal?.dueDate) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${currentScreen.singular}")},
        text = {
            Column {
                OutlinedTextField(
                    value = goalNameField,
                    onValueChange = { goalNameField = it },
                    label = {Text("Enter ${currentScreen.singular}")},
                    modifier = Modifier.focusRequester(focusRequester)
                )
                Spacer(modifier = Modifier.height(18.dp))
                DatePickerField(
                    defaultDate = pickedDate,
                    onDateSelected = { date ->
                        pickedDate = date
                    }
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
                    goal?.copy(
                        name = goalNameField.text,
                        dueDate = pickedDate
                    ) ?: Goal(
                        name = goalNameField.text,
                        dueDate = pickedDate
                    )
                )
            },
                enabled = !isDuplicate && goalNameField.text.isNotBlank()
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