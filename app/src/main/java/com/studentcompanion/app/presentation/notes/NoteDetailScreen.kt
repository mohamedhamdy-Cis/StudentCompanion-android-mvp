package com.studentcompanion.app.presentation.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** View / edit / delete a single note (per user story: student can edit or delete their notes). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    onBack: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) { viewModel.load(noteId) }
    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Note Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = viewModel::startEditing) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize()) {
            val note = state.note
            if (note == null) {
                CircularProgressIndicator()
                return@Column
            }

            if (state.isEditing) {
                OutlinedTextField(
                    value = state.editTitle,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.editBody,
                    onValueChange = viewModel::onBodyChange,
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = viewModel::saveEdit, modifier = Modifier.fillMaxWidth()) { Text("Save changes") }
            } else {
                Text(note.subjectName, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                Text(note.title, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                Text(note.body, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this note?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; viewModel.delete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }
}
