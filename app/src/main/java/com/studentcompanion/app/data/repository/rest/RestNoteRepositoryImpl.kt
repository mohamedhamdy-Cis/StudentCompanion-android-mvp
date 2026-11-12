package com.studentcompanion.app.data.repository.rest

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.core.util.safeCall
import com.studentcompanion.app.data.remote.rest.RestDataRefreshBus
import com.studentcompanion.app.data.remote.rest.StudentCompanionApi
import com.studentcompanion.app.data.remote.rest.dto.CreateNoteRequest
import com.studentcompanion.app.data.remote.rest.dto.UpdateNoteRequest
import com.studentcompanion.app.data.remote.rest.toDomain
import com.studentcompanion.app.domain.model.Note
import com.studentcompanion.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class RestNoteRepositoryImpl @Inject constructor(
    private val api: StudentCompanionApi,
    private val refreshBus: RestDataRefreshBus,
) : NoteRepository {

    override fun getNotes(studentId: String, subjectId: String?): Flow<List<Note>> =
        refreshBus.changes.onStart { emit(Unit) }
            .map { api.getNotes(subjectId).map { it.toDomain() } }
            .catch { emit(emptyList()) }

    override fun getNote(noteId: String): Flow<Note?> =
        refreshBus.changes.onStart { emit(Unit) }
            .map { runCatching { api.getNote(noteId).toDomain() }.getOrNull() }

    override suspend fun addNote(
        studentId: String,
        subjectId: String,
        subjectName: String,
        title: String,
        body: String,
    ): Result<Note> = safeCall("Couldn't save the note") {
        api.createNote(CreateNoteRequest(subjectId, title, body)).toDomain()
    }.also { if (it is Result.Success) refreshBus.notifyChanged() }

    override suspend fun updateNote(noteId: String, title: String, body: String): Result<Unit> =
        safeCall("Couldn't update the note") {
            api.updateNote(noteId, UpdateNoteRequest(title, body))
            Unit
        }.also { if (it is Result.Success) refreshBus.notifyChanged() }

    override suspend fun deleteNote(noteId: String): Result<Unit> =
        safeCall("Couldn't delete the note") { api.deleteNote(noteId) }
            .also { if (it is Result.Success) refreshBus.notifyChanged() }
}
