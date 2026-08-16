package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text

@Composable
fun DeleteHabitDialog(
    habitName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
){
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Let '$habitName' from your habits?") },
        confirmButton = {

            TextButton(onClick = { onConfirm()}) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Nooooo!")
            }
        }
    )
}