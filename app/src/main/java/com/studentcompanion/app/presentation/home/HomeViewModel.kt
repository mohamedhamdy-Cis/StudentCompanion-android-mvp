package com.studentcompanion.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentcompanion.app.domain.model.PointsSummary
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.usecase.auth.ObserveCurrentUserUseCase
import com.studentcompanion.app.domain.usecase.points.GetPointsSummaryUseCase
import com.studentcompanion.app.domain.usecase.task.EnsureTodayPrayerTasksUseCase
import com.studentcompanion.app.domain.usecase.task.GetDailyTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val studentName: String = "",
    val tasks: List<Task> = emptyList(),
    val pointsSummary: PointsSummary? = null,
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    private val getDailyTasksUseCase: GetDailyTasksUseCase,
    private val getPointsSummaryUseCase: GetPointsSummaryUseCase,
    private val ensureTodayPrayerTasksUseCase: EnsureTodayPrayerTasksUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val currentUser = observeCurrentUser()
        .filterNotNull()
        .distinctUntilChanged { old, new -> old.id == new.id }

    init {
        currentUser
            .onEach { user ->
                viewModelScope.launch { ensureTodayPrayerTasksUseCase(user.id) }
            }
            .flatMapLatest { user ->
                combine(
                    getDailyTasksUseCase(user.id),
                    getPointsSummaryUseCase(user.id),
                ) { tasks, points ->
                    HomeUiState(
                        studentName = user.fullName.ifBlank { user.username },
                        tasks = tasks,
                        pointsSummary = points,
                        isLoading = false,
                    )
                }
            }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }
}
