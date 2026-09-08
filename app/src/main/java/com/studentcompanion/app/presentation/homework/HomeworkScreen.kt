package com.studentcompanion.app.presentation.homework

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.model.TaskStatus

@Composable
fun HomeworkScreen(
    onOpenSubmit: (String) -> Unit,
    viewModel: HomeworkViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Homework", style = MaterialTheme.typography.headlineMedium)
        Text(
            "${state.homeworkTasks.count { it.status == TaskStatus.PENDING }} tasks pending",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.homeworkTasks) { task -> HomeworkCard(task = task, onClick = { onOpenSubmit(task.id) }) }
        }
    }
}

@Composable
private fun HomeworkCard(task: Task, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(task.subjectName.orEmpty().uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(task.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                AssistChip(onClick = {}, label = { Text(task.status.name) })
                Text("+${task.pointsValue} pts", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
