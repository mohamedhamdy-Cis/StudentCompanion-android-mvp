package com.studentcompanion.app.presentation.parent.childdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentcompanion.app.domain.model.PointsSummary
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.usecase.parent.GetChildPointsSummaryUseCase
import com.studentcompanion.app.domain.usecase.parent.GetChildTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class ChildDetailUiState(
    val tasks: List<Task> = emptyList(),
    val pointsSummary: PointsSummary? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ChildDetailViewModel @Inject constructor(
    private val getChildTasksUseCase: GetChildTasksUseCase,
    private val getChildPointsSummaryUseCase: GetChildPointsSummaryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildDetailUiState())
    val uiState: StateFlow<ChildDetailUiState> = _uiState.asStateFlow()

    fun load(studentId: String) {
        combine(
            getChildTasksUseCase(studentId),
            getChildPointsSummaryUseCase(studentId),
        ) { tasks, points ->
            ChildDetailUiState(tasks = tasks, pointsSummary = points, isLoading = false)
        }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }
}
