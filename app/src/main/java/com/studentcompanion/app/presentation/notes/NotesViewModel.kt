package com.studentcompanion.app.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.Note
import com.studentcompanion.app.domain.model.Subject
import com.studentcompanion.app.domain.usecase.auth.ObserveCurrentUserUseCase
import com.studentcompanion.app.domain.usecase.note.AddNoteUseCase
import com.studentcompanion.app.domain.usecase.note.DeleteNoteUseCase
import com.studentcompanion.app.domain.usecase.note.GetNoteDetailUseCase
import com.studentcompanion.app.domain.usecase.note.GetNotesUseCase
import com.studentcompanion.app.domain.usecase.note.UpdateNoteUseCase
import com.studentcompanion.app.domain.usecase.subject.GetSubjectsUseCase
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val studentId: String = "",
    val isLoading: Boolean = true,
    val addError: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val getSubjectsUseCase: GetSubjectsUseCase,
    private val addNoteUseCase: AddNoteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
            .map { it?.id }
            .filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { studentId ->
                combine(getNotesUseCase(studentId), getSubjectsUseCase(studentId)) { notes, subjects ->
                    NotesUiState(
                        notes = notes,
                        subjects = subjects,
                        studentId = studentId,
                        isLoading = false,
                    )
                }
            }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    fun addNote(subjectId: String, subjectName: String, title: String, body: String) {
        val studentId = _uiState.value.studentId
        if (studentId.isBlank()) return
        viewModelScope.launch {
            when (val result = addNoteUseCase(studentId, subjectId, subjectName, title, body)) {
                is Result.Success -> _uiState.update { it.copy(addError = null) }
                is Result.Error -> _uiState.update { it.copy(addError = result.message) }
                Result.Loading -> Unit
            }
        }
    }
}

data class NoteDetailUiState(
    val note: Note? = null,
    val isEditing: Boolean = false,
    val editTitle: String = "",
    val editBody: String = "",
    val isDeleted: Boolean = false,
)

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val getNoteDetailUseCase: GetNoteDetailUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    fun load(noteId: String) {
        getNoteDetailUseCase(noteId)
            .onEach { note -> _uiState.value = _uiState.value.copy(note = note, editTitle = note?.title.orEmpty(), editBody = note?.body.orEmpty()) }
            .launchIn(viewModelScope)
    }

    fun startEditing() = _uiState.update { it.copy(isEditing = true) }
    fun onTitleChange(value: String) = _uiState.update { it.copy(editTitle = value) }
    fun onBodyChange(value: String) = _uiState.update { it.copy(editBody = value) }

    fun saveEdit() {
        val note = _uiState.value.note ?: return
        viewModelScope.launch {
            when (updateNoteUseCase(note.id, _uiState.value.editTitle, _uiState.value.editBody)) {
                is Result.Success -> _uiState.update { it.copy(isEditing = false) }
                is Result.Error -> Unit
                Result.Loading -> Unit
            }
        }
    }

    fun delete() {
        val note = _uiState.value.note ?: return
        viewModelScope.launch {
            when (deleteNoteUseCase(note.id)) {
                is Result.Success -> _uiState.update { it.copy(isDeleted = true) }
                is Result.Error -> Unit
                Result.Loading -> Unit
            }
        }
    }
}
