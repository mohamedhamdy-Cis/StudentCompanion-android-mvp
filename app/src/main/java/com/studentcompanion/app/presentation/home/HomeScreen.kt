package com.studentcompanion.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.model.TaskStatus
import com.studentcompanion.app.domain.model.TaskType

/** Home screen: unified prayer + homework timeline, points chip, streak card. */
@Composable
fun HomeScreen(
    onOpenNotes: () -> Unit,
    onOpenHomework: () -> Unit,
    onOpenPoints: () -> Unit,
    onOpenTask: (String) -> Unit,
    onOpenShareCode: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Assalamu alaikum,", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(state.studentName.ifBlank { "Student" }, style = MaterialTheme.typography.headlineMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenShareCode) {
                    Icon(Icons.Default.Share, contentDescription = "Share with a parent")
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp).clickable { onOpenPoints() },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${state.pointsSummary?.totalPoints ?: 0} pts", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        Text(
            "Today's Timeline",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.tasks) { task -> TaskRow(task = task, onClick = { onOpenTask(task.id) }) }
        }

        NavigationBar {
            NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
            NavigationBarItem(selected = false, onClick = onOpenNotes, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Notes") })
            NavigationBarItem(selected = false, onClick = onOpenHomework, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Homework") })
            NavigationBarItem(selected = false, onClick = onOpenPoints, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Points") })
        }
    }
}

@Composable
private fun TaskRow(task: Task, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    task.subjectName ?: if (task.type == TaskType.PRAYER) "Prayer" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val label = when (task.status) {
                    TaskStatus.PENDING -> "Pending"
                    TaskStatus.SUBMITTED -> "Submitted"
                    TaskStatus.APPROVED -> "Done"
                    TaskStatus.NEEDS_REVISION -> "Revise"
                }
                Text(label, style = MaterialTheme.typography.labelMedium)
                Text("+${task.pointsValue} pts", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
