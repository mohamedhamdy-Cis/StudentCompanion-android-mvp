package com.studentcompanion.app.presentation.task

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studentcompanion.app.domain.model.TaskStatus
import com.studentcompanion.app.domain.model.TaskType
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onBack: () -> Unit,
    onSubmitHomework: (String) -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) { viewModel.load(taskId) }
    LaunchedEffect(state.completed) { if (state.completed) onBack() }

    val task = state.task
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(task?.title ?: "Task") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (task == null) {
            CircularProgressIndicator(modifier = Modifier.padding(padding).padding(24.dp))
            return@Scaffold
        }

        val dueLabel = DateTimeFormatter.ofPattern("h:mm a")
            .format(task.dueAt.atZone(ZoneId.of("Asia/Riyadh")))

        Column(modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize()) {
            Text(
                if (task.type == TaskType.PRAYER) "Prayer" else task.subjectName.orEmpty(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(task.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Due $dueLabel · +${task.pointsValue} pts", color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (task.description.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(task.description, style = MaterialTheme.typography.bodyLarge)
            }

            task.teacherComment?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(16.dp))
                Text("Teacher feedback", style = MaterialTheme.typography.titleMedium)
                Text(it, style = MaterialTheme.typography.bodyLarge)
            }

            if (task.status == TaskStatus.PENDING) {
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = state.comment,
                    onValueChange = viewModel::onCommentChange,
                    label = { Text(if (task.type == TaskType.PRAYER) "Optional note" else "Comment for your teacher") },
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                )
            }

            state.errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.weight(1f))

            when {
                task.type == TaskType.HOMEWORK && task.status == TaskStatus.PENDING -> {
                    Button(
                        onClick = { onSubmitHomework(task.id) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                    ) { Text("Submit homework") }
                }
                task.type == TaskType.PRAYER && task.status == TaskStatus.PENDING -> {
                    Button(
                        onClick = viewModel::markComplete,
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                    ) {
                        if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Mark as prayed")
                    }
                }
                else -> {
                    OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
