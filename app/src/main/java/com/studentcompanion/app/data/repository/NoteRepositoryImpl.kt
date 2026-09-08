package com.studentcompanion.app.data.repository

import com.google.firebase.Timestamp
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.data.mapper.toDomain
import com.studentcompanion.app.data.model.NoteDto
import com.studentcompanion.app.data.remote.firebase.FirestoreNoteDataSource
import com.studentcompanion.app.domain.model.Note
import com.studentcompanion.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDataSource: FirestoreNoteDataSource,
) : NoteRepository {

    override fun getNotes(studentId: String, subjectId: String?): Flow<List<Note>> =
        noteDataSource.observeNotes(studentId, subjectId).map { list -> list.map { it.toDomain() } }

    override fun getNote(noteId: String): Flow<Note?> =
        noteDataSource.observeNote(noteId).map { it?.toDomain() }

    override suspend fun addNote(studentId: String, subjectId: String, subjectName: String, title: String, body: String): Result<Note> = try {
        val now = Timestamp.now()
        val dto = NoteDto(
            studentId = studentId,
            subjectId = subjectId,
            subjectName = subjectName,
            title = title,
            body = body,
            createdAt = now,
            updatedAt = now,
        )
        Result.Success(noteDataSource.addNote(dto).toDomain())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't save the note", e)
    }

    override suspend fun updateNote(noteId: String, title: String, body: String): Result<Unit> = try {
        noteDataSource.updateNote(noteId, title, body, Timestamp.now())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't update the note", e)
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> = try {
        noteDataSource.deleteNote(noteId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't delete the note", e)
    }
}
