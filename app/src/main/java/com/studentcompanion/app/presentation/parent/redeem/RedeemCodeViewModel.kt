package com.studentcompanion.app.presentation.parent.redeem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.usecase.parent.RedeemLinkCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RedeemCodeUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val linkedChildName: String? = null,
)

@HiltViewModel
class RedeemCodeViewModel @Inject constructor(
    private val redeemLinkCodeUseCase: RedeemLinkCodeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RedeemCodeUiState())
    val uiState: StateFlow<RedeemCodeUiState> = _uiState.asStateFlow()

    fun onCodeChange(value: String) =
        _uiState.update { it.copy(code = value.uppercase(), errorMessage = null) }

    fun redeem() {
        val code = _uiState.value.code
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = redeemLinkCodeUseCase(code)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, linkedChildName = result.data.fullName.ifBlank { result.data.username })
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                Result.Loading -> Unit
            }
        }
    }
}
