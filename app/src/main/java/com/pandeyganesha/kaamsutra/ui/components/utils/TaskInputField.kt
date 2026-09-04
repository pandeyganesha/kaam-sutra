package com.pandeyganesha.kaamsutra.ui.components.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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

    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    val padding = 12.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp)
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
                    val result = layoutResult ?: return@drawBehind
                    if (result.lineCount <= 1) return@drawBehind
                    val y = result.getLineBottom(0)
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
        if (!titleEmpty && descriptionEmpty) {
            val firstLineBottom = layoutResult?.getLineBottom(0) ?: 0f
            Text(
                text = "Description (optional)",
                color = placeholderColor,
                modifier = Modifier
                    .padding(padding)
                    .offset { IntOffset(x = 0, y = firstLineBottom.roundToInt()) }
            )
        }
    }
}