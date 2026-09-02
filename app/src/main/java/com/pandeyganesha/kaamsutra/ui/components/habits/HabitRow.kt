package com.pandeyganesha.kaamsutra.ui.components.habits

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.style.TextDecoration
import com.pandeyganesha.kaamsutra.data.Habit
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput


@Composable
fun HabitRow(
    habit: Habit,
    showRepeatTypeOrDays: Boolean,
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
                    onDoubleTap = { onCheckedChange(!isChecked) },
                    onTap = {onEditClick()}
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
                text = habit.name.trim(),
                textDecoration = if (isChecked) {
                    TextDecoration.LineThrough
                }
                else {
                    TextDecoration.None
            } )

            if (showRepeatTypeOrDays) {
                Text(
                    text = habit.repeatType.displayName,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFFB8B8B8)
                )
            }
        }
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }

}