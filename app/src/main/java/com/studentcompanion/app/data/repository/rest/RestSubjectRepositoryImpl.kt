package com.studentcompanion.app.data.repository.rest

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.core.util.safeCall
import com.studentcompanion.app.data.remote.rest.RestDataRefreshBus
import com.studentcompanion.app.data.remote.rest.StudentCompanionApi
import com.studentcompanion.app.data.remote.rest.dto.CreateSubjectRequest
import com.studentcompanion.app.data.remote.rest.toDomain
import com.studentcompanion.app.domain.model.Subject
import com.studentcompanion.app.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

// The backend derives the student from the bearer token, so [studentId]
// here is unused on the wire — kept only to satisfy the domain interface,
// which still carries it from the original per-student query design.
class RestSubjectRepositoryImpl @Inject constructor(
    private val api: StudentCompanionApi,
    private val refreshBus: RestDataRefreshBus,
) : SubjectRepository {

    override fun getSubjects(studentId: String): Flow<List<Subject>> =
        refreshBus.changes.onStart { emit(Unit) }
            .map { api.getSubjects().map { it.toDomain() } }
            .catch { emit(emptyList()) }

    override suspend fun addSubject(studentId: String, name: String): Result<Subject> =
        safeCall("Couldn't add subject") {
            api.createSubject(CreateSubjectRequest(name)).toDomain()
        }.also { if (it is Result.Success) refreshBus.notifyChanged() }
}
