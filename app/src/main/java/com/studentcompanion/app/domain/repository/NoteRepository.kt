package com.studentcompanion.app.domain.repository

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(studentId: String, subjectId: String? = null): Flow<List<Note>>
    fun getNote(noteId: String): Flow<Note?>
    suspend fun addNote(studentId: String, subjectId: String, subjectName: String, title: String, body: String): Result<Note>
    suspend fun updateNote(noteId: String, title: String, body: String): Result<Unit>
    suspend fun deleteNote(noteId: String): Result<Unit>
}
