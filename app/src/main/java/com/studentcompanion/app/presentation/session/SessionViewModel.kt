package com.studentcompanion.app.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentcompanion.app.domain.model.UserRole
import com.studentcompanion.app.domain.usecase.auth.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SessionUiState(
    val isReady: Boolean = false,
    val isLoggedIn: Boolean = false,
    val displayName: String = "",
    val role: UserRole? = null,
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
) : ViewModel() {

    val uiState = observeCurrentUser()
        .map { user ->
            SessionUiState(
                isReady = true,
                isLoggedIn = user != null,
                displayName = user?.fullName.orEmpty(),
                role = user?.role,
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionUiState())
}
