package com.studentcompanion.app.presentation.points

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PointsScreen(viewModel: PointsViewModel = hiltViewModel()) {
    val summary by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(vertical = 28.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("TOTAL POINTS", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                Text("${summary?.totalPoints ?: 0}", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium)
                Text("🔥 ${summary?.currentStreakDays ?: 0}-day streak", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("This Week", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn {
            items(summary?.recentEntries.orEmpty()) { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(entry.label)
                    Text("+${entry.points}", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
