package com.studentcompanion.app.domain.usecase.note

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.Note
import com.studentcompanion.app.domain.repository.NoteRepository
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke(studentId: String, subjectId: String, subjectName: String, title: String, body: String): Result<Note> {
        if (title.isBlank()) return Result.Error("Note title can't be empty")
        return noteRepository.addNote(studentId, subjectId, subjectName, title.trim(), body.trim())
    }
}
