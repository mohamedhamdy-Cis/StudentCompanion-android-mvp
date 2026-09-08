package com.studentcompanion.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.usecase.auth.ObserveCurrentUserUseCase
import com.studentcompanion.app.domain.usecase.subject.SaveSubjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val subjects: List<String> = listOf("Mathematics", "Science", "Arabic", "Quran", "English"),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val saveSubjectsUseCase: SaveSubjectsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun continueOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val user = observeCurrentUser().first()
            if (user == null) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "You're not signed in") }
                return@launch
            }
            when (val result = saveSubjectsUseCase(user.id, _uiState.value.subjects)) {
                is Result.Success -> _uiState.update { it.copy(isSaving = false, saved = true) }
                is Result.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                Result.Loading -> Unit
            }
        }
    }
}
