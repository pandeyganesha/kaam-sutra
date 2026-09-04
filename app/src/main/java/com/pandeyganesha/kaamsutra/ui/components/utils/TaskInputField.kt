package com.pandeyganesha.kaamsutra.ui.components.utils

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val InitialHeight = 56.dp
private val ExpandedDefaultHeight = 120.dp
private val MaxHeight = 220.dp

@Composable
fun TaskInputField(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    focusRequester: FocusRequester? = null,
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val hasBody = text.contains('\n')
    val titleEmpty = text.substringBefore('\n').isEmpty()
    val descriptionEmpty = !hasBody || text.substringAfter('\n').isEmpty()

    // Index right where the title ends (the '\n', or end-of-text if no body yet).
    val titleEndIndex = text.indexOf('\n').let { if (it == -1) text.length else it }

    // Bottom of the title's LAST visual line. A long title can wrap across two+ lines
    // even with zero '\n' — line 0 alone is wrong for that case, which was the overlap bug.
    val titleLastLineBottom = layoutResult?.let { result ->
        val line = result.getLineForOffset(titleEndIndex.coerceIn(0, text.length))
        result.getLineBottom(line)
    }

    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    val padding = 12.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(4.dp))
                .heightIn(
                    min = if (hasBody) ExpandedDefaultHeight else InitialHeight,
                    max = MaxHeight,
                )
                .verticalScroll(rememberScrollState())
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .then(
                        if (focusRequester != null) Modifier.focusRequester(focusRequester)
                        else Modifier
                    )
                    .drawBehind {
                        if (!hasBody) return@drawBehind
                        val y = titleLastLineBottom ?: return@drawBehind
                        drawLine(
                            color = dividerColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    },
                textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
                cursorBrush = SolidColor(LocalContentColor.current),
                onTextLayout = { layoutResult = it },
            )

            if (titleEmpty) {
                Text(text = "Title", color = placeholderColor, modifier = Modifier.padding(padding))
            }
            if (!titleEmpty && descriptionEmpty && hasBody) {
                val y = titleLastLineBottom ?: 0f
                Text(
                    text = "Description (optional)",
                    color = placeholderColor,
                    modifier = Modifier
                        .padding(padding)
                        .offset { IntOffset(x = 0, y = y.roundToInt()) }
                )
            }
        }

        if (!hasBody && !titleEmpty) {
            Text(
                text = "Press enter to enter description as well",
                style = MaterialTheme.typography.bodySmall,
                color = placeholderColor,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}