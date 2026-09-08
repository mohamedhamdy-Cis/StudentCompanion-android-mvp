package com.studentcompanion.app.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.studentcompanion.app.core.util.Constants
import com.studentcompanion.app.data.model.NoteDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreNoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val notes get() = firestore.collection(Constants.COLLECTION_NOTES)

    fun observeNotes(studentId: String, subjectId: String?): Flow<List<NoteDto>> = callbackFlow {
        var query = notes.whereEqualTo("studentId", studentId)
        if (subjectId != null) query = query.whereEqualTo("subjectId", subjectId)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                doc.toObject(NoteDto::class.java)?.copy(id = doc.id)
            }
            trySend(items)
        }
        awaitClose { registration.remove() }
    }

    fun observeNote(noteId: String): Flow<NoteDto?> = callbackFlow {
        val registration = notes.document(noteId).addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snapshot?.toObject(NoteDto::class.java)?.copy(id = snapshot.id))
        }
        awaitClose { registration.remove() }
    }

    suspend fun addNote(dto: NoteDto): NoteDto {
        val docRef = notes.document()
        val withId = dto.copy(id = docRef.id)
        docRef.set(withId).await()
        return withId
    }

    suspend fun updateNote(noteId: String, title: String, body: String, updatedAt: com.google.firebase.Timestamp) {
        notes.document(noteId).update(
            mapOf("title" to title, "body" to body, "updatedAt" to updatedAt)
        ).await()
    }

    suspend fun deleteNote(noteId: String) {
        notes.document(noteId).delete().await()
    }
}
