package com.studentcompanion.app.domain.usecase.note

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.repository.NoteRepository
import javax.inject.Inject

class UpdateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke(noteId: String, title: String, body: String): Result<Unit> {
        if (title.isBlank()) return Result.Error("Note title can't be empty")
        return noteRepository.updateNote(noteId, title.trim(), body.trim())
    }
}
