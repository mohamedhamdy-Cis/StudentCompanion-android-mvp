package com.studentcompanion.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.studentcompanion.app.core.util.Constants
import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.Subject
import com.studentcompanion.app.domain.repository.SubjectRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SubjectRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : SubjectRepository {

    private val subjects get() = firestore.collection(Constants.COLLECTION_SUBJECTS)

    override fun getSubjects(studentId: String): Flow<List<Subject>> = callbackFlow {
        val registration = subjects.whereEqualTo("studentId", studentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(Subject::class.java)?.copy(id = doc.id)
                })
            }
        awaitClose { registration.remove() }
    }

    override suspend fun addSubject(studentId: String, name: String): Result<Subject> = try {
        val docRef = subjects.document()
        val subject = Subject(id = docRef.id, name = name, studentId = studentId)
        docRef.set(subject).await()
        Result.Success(subject)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't add subject", e)
    }
}
