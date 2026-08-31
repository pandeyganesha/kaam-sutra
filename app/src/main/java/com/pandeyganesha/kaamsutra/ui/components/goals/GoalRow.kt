package com.pandeyganesha.kaamsutra.ui.components.goals

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.pandeyganesha.kaamsutra.data.Goal
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date


@Composable
fun GoalRow(
    goal: Goal,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .pointerInput(isChecked) {
                detectTapGestures(
                    onDoubleTap = { onCheckedChange(!isChecked) }
                )
            },
        verticalAlignment = Alignment.CenterVertically

    ){
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = goal.name.trim(),
                textDecoration = if (isChecked) {
                    TextDecoration.LineThrough
                }
                else {
                    TextDecoration.None
                } )

            Text(
                text = goal.dueDate?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                } ?: "No due date",
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = Color(0xFFB8B8B8)
            )

        }
        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }

}