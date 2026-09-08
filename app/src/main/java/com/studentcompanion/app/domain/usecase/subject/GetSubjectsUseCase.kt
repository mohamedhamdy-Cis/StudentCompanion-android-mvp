package com.studentcompanion.app.domain.usecase.subject

import com.studentcompanion.app.domain.model.Subject
import com.studentcompanion.app.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSubjectsUseCase @Inject constructor(
    private val subjectRepository: SubjectRepository,
) {
    operator fun invoke(studentId: String): Flow<List<Subject>> = subjectRepository.getSubjects(studentId)
}
