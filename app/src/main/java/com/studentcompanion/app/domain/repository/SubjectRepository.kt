package com.studentcompanion.app.domain.repository

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    fun getSubjects(studentId: String): Flow<List<Subject>>
    suspend fun addSubject(studentId: String, name: String): Result<Subject>
}
