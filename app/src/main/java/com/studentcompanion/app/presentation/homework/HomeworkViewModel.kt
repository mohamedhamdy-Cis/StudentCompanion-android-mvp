package com.studentcompanion.app.presentation.homework

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.model.TaskType
import com.studentcompanion.app.domain.usecase.auth.ObserveCurrentUserUseCase
import com.studentcompanion.app.domain.usecase.task.GetDailyTasksUseCase
import com.studentcompanion.app.domain.usecase.task.SubmitHomeworkUseCase
import com.studentcompanion.app.domain.usecase.task.ValidateAttachmentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeworkUiState(val homeworkTasks: List<Task> = emptyList(), val isLoading: Boolean = true)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeworkViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    getDailyTasksUseCase: GetDailyTasksUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeworkUiState())
    val uiState: StateFlow<HomeworkUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
            .map { it?.id }
            .filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { studentId -> getDailyTasksUseCase(studentId) }
            .map { tasks -> tasks.filter { it.type == TaskType.HOMEWORK } }
            .onEach { homework -> _uiState.value = HomeworkUiState(homeworkTasks = homework, isLoading = false) }
            .launchIn(viewModelScope)
    }
}

const val MAX_HOMEWORK_IMAGES = 5

data class HomeworkSubmitUiState(
    val taskId: String = "",
    val comment: String = "",
    val selectedImages: List<Uri> = emptyList(),
    val attachmentError: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitSuccess: Boolean = false,
)

@HiltViewModel
class HomeworkSubmitViewModel @Inject constructor(
    private val validateAttachmentUseCase: ValidateAttachmentUseCase,
    private val submitHomeworkUseCase: SubmitHomeworkUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeworkSubmitUiState())
    val uiState: StateFlow<HomeworkSubmitUiState> = _uiState.asStateFlow()

    fun setTaskId(taskId: String) = _uiState.update { it.copy(taskId = taskId) }
    fun onCommentChange(value: String) = _uiState.update { it.copy(comment = value) }

    /** Validates each newly picked image against the 5 MB limit before adding it to the list. */
    fun onImagesPicked(uris: List<Uri>) {
        val current = _uiState.value.selectedImages
        val room = MAX_HOMEWORK_IMAGES - current.size
        if (room <= 0) {
            _uiState.update { it.copy(attachmentError = "You can attach up to $MAX_HOMEWORK_IMAGES images") }
            return
        }
        val candidates = uris.take(room)
        val accepted = mutableListOf<Uri>()
        var firstError: String? = null
        for (uri in candidates) {
            when (val result = validateAttachmentUseCase(uri)) {
                is Result.Success -> accepted += uri
                is Result.Error -> if (firstError == null) firstError = result.message
                Result.Loading -> Unit
            }
        }
        _uiState.update { it.copy(selectedImages = it.selectedImages + accepted, attachmentError = firstError) }
    }

    fun removeImage(uri: Uri) = _uiState.update { it.copy(selectedImages = it.selectedImages - uri, attachmentError = null) }

    fun submit() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            when (val result = submitHomeworkUseCase(state.taskId, state.comment.ifBlank { null }, state.selectedImages)) {
                is Result.Success -> _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
                is Result.Error -> _uiState.update { it.copy(isSubmitting = false, submitError = result.message) }
                Result.Loading -> Unit
            }
        }
    }
}
