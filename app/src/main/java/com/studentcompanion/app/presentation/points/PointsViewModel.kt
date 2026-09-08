package com.studentcompanion.app.presentation.points

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentcompanion.app.domain.model.PointsSummary
import com.studentcompanion.app.domain.usecase.auth.ObserveCurrentUserUseCase
import com.studentcompanion.app.domain.usecase.points.GetPointsSummaryUseCase
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
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PointsViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    getPointsSummaryUseCase: GetPointsSummaryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PointsSummary?>(null)
    val uiState: StateFlow<PointsSummary?> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
            .map { it?.id }
            .filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { studentId -> getPointsSummaryUseCase(studentId) }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }
}
