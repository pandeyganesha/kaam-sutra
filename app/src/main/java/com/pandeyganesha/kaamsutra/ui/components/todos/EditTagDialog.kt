package com.pandeyganesha.kaamsutra.ui.components.todos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import com.pandeyganesha.kaamsutra.data.Tag
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun EditTagDialog(
    tags: List<Tag> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (createdTags: List<Tag>, deletedTags: List<Tag>) -> Unit,
    modifier: Modifier = Modifier
) {
    val editedTags = remember { mutableStateListOf<Tag>().apply { addAll(tags) } }
    val createdTags = remember { mutableStateListOf<Tag>() }
    val deletedTags = remember { mutableStateListOf<Tag>() }
    val focusRequester = remember { FocusRequester() }
    var newTag by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Create Tags",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 10.dp)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                editedTags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = { },
                        label = { Text(tag.name) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove tag",
                                modifier = Modifier
                                    .size(InputChipDefaults.AvatarSize)
                                    .clickable {
                                        editedTags.remove(tag)
                                        // only mark as "deleted" if it existed in DB already;
                                        // if it was just created in this session, cancel it out instead
                                        if (createdTags.remove(tag)) {
                                            // it was a fresh, unsaved tag — nothing to delete from DB
                                        } else {
                                            deletedTags.add(tag)
                                        }
                                    }
                            )
                        }
                    )
                }
            }
            OutlinedTextField(
                value = newTag,
                onValueChange = { newTag = it },
                label = { Text("Tag") },
                trailingIcon = {
                    IconButton(onClick = {
                        if (newTag.isNotBlank()) {
                            val tag = Tag(name = newTag)
                            editedTags.add(tag)
                            createdTags.add(tag)
                            newTag = ""
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Add tag")
                    }
                },
                modifier = Modifier.focusRequester(focusRequester)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            onConfirm(createdTags.toList(), deletedTags.toList())
                        }
                        .padding(top = 38.dp, end = 20.dp)
                )
            }
        }
    }
}