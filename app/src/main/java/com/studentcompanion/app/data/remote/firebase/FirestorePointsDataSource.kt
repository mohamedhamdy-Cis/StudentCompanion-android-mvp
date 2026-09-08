package com.studentcompanion.app.data.remote.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.studentcompanion.app.core.util.Constants
import com.studentcompanion.app.data.model.PointsEntryDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePointsDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val ledger get() = firestore.collection(Constants.COLLECTION_POINTS_LEDGER)

    fun observeLedger(studentId: String): Flow<List<PointsEntryDto>> = callbackFlow {
        val query = ledger.whereEqualTo("studentId", studentId)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                doc.toObject(PointsEntryDto::class.java)?.copy(id = doc.id)
            }
            trySend(items)
        }
        awaitClose { registration.remove() }
    }

    suspend fun awardPoints(studentId: String, taskId: String?, label: String, points: Int) {
        if (taskId != null) {
            val existing = ledger.whereEqualTo("taskId", taskId).limit(1).get().await()
            if (!existing.isEmpty) return
        }
        val docRef = ledger.document()
        val dto = PointsEntryDto(
            id = docRef.id,
            studentId = studentId,
            taskId = taskId,
            label = label,
            points = points,
            earnedAt = Timestamp.now(),
        )
        docRef.set(dto).await()
    }
}
