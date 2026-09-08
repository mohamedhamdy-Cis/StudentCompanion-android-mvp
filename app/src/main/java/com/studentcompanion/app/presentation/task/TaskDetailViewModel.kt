package com.studentcompanion.app.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.usecase.task.CompleteTaskUseCase
import com.studentcompanion.app.domain.usecase.task.GetTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDetailUiState(
    val task: Task? = null,
    val comment: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val completed: Boolean = false,
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val getTaskUseCase: GetTaskUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    fun load(taskId: String) {
        getTaskUseCase(taskId)
            .onEach { task ->
                _uiState.update { state ->
                    state.copy(
                        task = task,
                        comment = if (state.comment.isBlank()) task?.studentComment.orEmpty() else state.comment,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onCommentChange(value: String) = _uiState.update { it.copy(comment = value, errorMessage = null) }

    fun markComplete() {
        val task = _uiState.value.task ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = completeTaskUseCase(task.id, _uiState.value.comment.ifBlank { null })) {
                is Result.Success -> _uiState.update { it.copy(isSaving = false, completed = true) }
                is Result.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                Result.Loading -> Unit
            }
        }
    }
}
