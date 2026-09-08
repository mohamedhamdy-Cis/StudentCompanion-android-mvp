package com.studentcompanion.app.domain.usecase.note

import com.studentcompanion.app.domain.model.Note
import com.studentcompanion.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNoteDetailUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
) {
    operator fun invoke(noteId: String): Flow<Note?> = noteRepository.getNote(noteId)
}
