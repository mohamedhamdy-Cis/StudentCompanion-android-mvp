package com.studentcompanion.app.domain.usecase.note

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.repository.NoteRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke(noteId: String): Result<Unit> = noteRepository.deleteNote(noteId)
}
