package com.studentcompanion.app.presentation.parent.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studentcompanion.app.domain.model.User

/** Parent's home screen: linked children, each tapping through to a read-only view of their tasks/points. */
@Composable
fun ParentHomeScreen(
    onOpenChild: (String) -> Unit,
    onLinkAnotherChild: () -> Unit,
    viewModel: ParentHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Welcome,", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(state.parentName.ifBlank { "Parent" }, style = MaterialTheme.typography.headlineMedium)
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.children.isEmpty() -> EmptyChildrenState(onLinkAnotherChild)
            else -> LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.children) { child -> ChildRow(child = child, onClick = { onOpenChild(child.id) }) }
                item {
                    OutlinedButton(
                        onClick = onLinkAnotherChild,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) { Text("Link another child") }
                }
            }
        }
    }
}

@Composable
private fun EmptyChildrenState(onLinkAnotherChild: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("No children linked yet", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Ask your child to generate a code from their app, then enter it here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onLinkAnotherChild) { Text("Enter a code") }
    }
}

@Composable
private fun ChildRow(child: User, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(child.fullName.ifBlank { child.username }, style = MaterialTheme.typography.titleMedium)
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}
