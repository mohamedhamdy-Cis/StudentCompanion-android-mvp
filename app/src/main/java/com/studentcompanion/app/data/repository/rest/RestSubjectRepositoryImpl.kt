package com.studentcompanion.app.data.repository.rest

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.data.remote.rest.StudentCompanionApi
import com.studentcompanion.app.data.remote.rest.dto.CreateSubjectRequest
import com.studentcompanion.app.data.remote.rest.toDomain
import com.studentcompanion.app.domain.model.Subject
import com.studentcompanion.app.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

// The backend derives the student from the bearer token, so [studentId]
// here is unused on the wire — kept only to satisfy the domain interface,
// which still carries it from the Firestore-era (per-student query) design.
class RestSubjectRepositoryImpl @Inject constructor(
    private val api: StudentCompanionApi,
) : SubjectRepository {

    override fun getSubjects(studentId: String): Flow<List<Subject>> = flow {
        emit(api.getSubjects().map { it.toDomain() })
    }

    override suspend fun addSubject(studentId: String, name: String): Result<Subject> = try {
        Result.Success(api.createSubject(CreateSubjectRequest(name)).toDomain())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Couldn't add subject", e)
    }
}
