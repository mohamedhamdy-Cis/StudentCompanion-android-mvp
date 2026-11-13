package com.studentcompanion.app.presentation.parent.sharecode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.LinkCode
import com.studentcompanion.app.domain.usecase.parent.GenerateLinkCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShareCodeUiState(
    val linkCode: LinkCode? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class ShareCodeViewModel @Inject constructor(
    private val generateLinkCodeUseCase: GenerateLinkCodeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareCodeUiState())
    val uiState: StateFlow<ShareCodeUiState> = _uiState.asStateFlow()

    init {
        generate()
    }

    fun generate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = generateLinkCodeUseCase()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, linkCode = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                Result.Loading -> Unit
            }
        }
    }
}
