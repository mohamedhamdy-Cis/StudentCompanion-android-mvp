package com.studentcompanion.app.presentation.homework

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.studentcompanion.app.core.util.Constants

/**
 * Homework submission: student attaches photo(s) of their completed work
 * (max 5, each capped at 5 MB — enforced client-side via
 * ValidateAttachmentUseCase before any upload starts) and submits.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkSubmitScreen(
    taskId: String,
    onSubmitted: () -> Unit,
    viewModel: HomeworkSubmitViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) { viewModel.setTaskId(taskId) }
    LaunchedEffect(state.submitSuccess) { if (state.submitSuccess) onSubmitted() }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris -> viewModel.onImagesPicked(uris) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit Homework") },
                navigationIcon = {
                    IconButton(onClick = onSubmitted) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp)) {
                Button(
                    onClick = viewModel::submit,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Submit Homework")
                    }
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 24.dp).fillMaxSize()) {
            Spacer(Modifier.height(8.dp))

            Text("Attach photos of your work", style = MaterialTheme.typography.titleMedium)
            Text(
                "Up to $MAX_HOMEWORK_IMAGES images, ${Constants.MAX_ATTACHMENT_SIZE_BYTES / (1024 * 1024)} MB max per image",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(state.selectedImages) { uri ->
                    Box(modifier = Modifier.aspectRatio(1f)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        )
                        IconButton(
                            onClick = { viewModel.removeImage(uri) },
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                        }
                    }
                }
                if (state.selectedImages.size < MAX_HOMEWORK_IMAGES) {
                    item {
                        Surface(
                            onClick = { imagePicker.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.aspectRatio(1f),
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add photo", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            state.attachmentError?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = state.comment,
                onValueChange = viewModel::onCommentChange,
                label = { Text("Comment for your teacher (optional)") },
                placeholder = { Text("e.g. \"I found question 3 tricky, please check my working\"") },
                modifier = Modifier.fillMaxWidth().height(110.dp),
            )

            state.submitError?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
