package com.studentcompanion.app.data.repository.rest

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.data.remote.rest.StudentCompanionApi
import com.studentcompanion.app.data.remote.rest.dto.CreateNoteRequest
import com.studentcompanion.app.data.remote.rest.dto.UpdateNoteRequest
import com.studentcompanion.app.data.remote.rest.toDomain
import com.studentcompanion.app.domain.model.Note
import com.studentcompanion.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RestNoteRepositoryImpl @Inject constructor(
    private val api: StudentCompanionApi,
) : NoteRepository {

    override fun getNotes(studentId: String, subjectId: String?): Flow<List<Note>> = flow {
        emit(api.getNotes(subjectId).map { it.toDomain() })
    }

    override fun getNote(noteId: String): Flow<Note?> = flow {
        emit(runCatching { api.getNote(noteId).toDomain() }.getOrNull())
    }

    override suspend fun addNote(
        studentId: String,
        subjectId: String,
        subjectName: String,
        title: String,
        body: String,
    ): Result<Note> = try {
        Result.Success(api.createNote(CreateNoteRequest(subjectId, title, body)).toDomain())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't save the note", e)
    }

    override suspend fun updateNote(noteId: String, title: String, body: String): Result<Unit> = try {
        api.updateNote(noteId, UpdateNoteRequest(title, body))
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't update the note", e)
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> = try {
        api.deleteNote(noteId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't delete the note", e)
    }
}
