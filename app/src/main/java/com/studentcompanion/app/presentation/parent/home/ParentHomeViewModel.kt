package com.studentcompanion.app.presentation.parent.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentcompanion.app.domain.model.User
import com.studentcompanion.app.domain.usecase.auth.ObserveCurrentUserUseCase
import com.studentcompanion.app.domain.usecase.parent.GetChildrenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class ParentHomeUiState(
    val parentName: String = "",
    val children: List<User> = emptyList(),
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ParentHomeViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    getChildrenUseCase: GetChildrenUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentHomeUiState())
    val uiState: StateFlow<ParentHomeUiState> = _uiState.asStateFlow()

    init {
        combine(observeCurrentUser(), getChildrenUseCase()) { user, children ->
            ParentHomeUiState(
                parentName = user?.fullName.orEmpty(),
                children = children,
                isLoading = false,
            )
        }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }
}
