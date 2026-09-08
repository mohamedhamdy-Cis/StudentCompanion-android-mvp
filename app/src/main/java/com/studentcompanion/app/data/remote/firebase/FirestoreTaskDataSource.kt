package com.studentcompanion.app.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.studentcompanion.app.core.util.Constants
import com.studentcompanion.app.data.model.TaskAttachmentDto
import com.studentcompanion.app.data.model.TaskDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreTaskDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val tasks get() = firestore.collection(Constants.COLLECTION_TASKS)

    fun observeDailyTasks(studentId: String): Flow<List<TaskDto>> = callbackFlow {
        // Sorted in the repository so we don't need a composite Firestore index for the MVP.
        val query = tasks.whereEqualTo("studentId", studentId)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                doc.toObject(TaskDto::class.java)?.copy(id = doc.id)
            }
            trySend(items)
        }
        awaitClose { registration.remove() }
    }

    fun observeTask(taskId: String): Flow<TaskDto?> = callbackFlow {
        val registration = tasks.document(taskId).addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snapshot?.toObject(TaskDto::class.java)?.copy(id = snapshot.id))
        }
        awaitClose { registration.remove() }
    }

    suspend fun createTask(dto: TaskDto): TaskDto {
        val docRef = tasks.document()
        val withId = dto.copy(id = docRef.id)
        docRef.set(withId).await()
        return withId
    }

    suspend fun markCompleted(taskId: String, comment: String?) {
        val updates = mutableMapOf<String, Any>("status" to "APPROVED")
        if (comment != null) updates["studentComment"] = comment
        tasks.document(taskId).update(updates).await()
    }

    suspend fun submitHomework(taskId: String, comment: String?, attachments: List<TaskAttachmentDto>) {
        val updates = mutableMapOf<String, Any>(
            "status" to "SUBMITTED",
            "attachments" to attachments.map { mapOf("id" to it.id, "url" to it.url, "sizeBytes" to it.sizeBytes) },
        )
        if (comment != null) updates["studentComment"] = comment
        tasks.document(taskId).update(updates).await()
    }
}
