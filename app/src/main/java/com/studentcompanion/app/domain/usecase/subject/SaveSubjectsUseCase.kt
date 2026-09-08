package com.studentcompanion.app.domain.usecase.subject

import com.studentcompanion.app.core.util.Result
import com.studentcompanion.app.domain.repository.SubjectRepository
import javax.inject.Inject

class SaveSubjectsUseCase @Inject constructor(
    private val subjectRepository: SubjectRepository,
) {
    suspend operator fun invoke(studentId: String, names: List<String>): Result<Unit> {
        val trimmed = names.map { it.trim() }.filter { it.isNotEmpty() }
        if (trimmed.isEmpty()) return Result.Error("Add at least one subject")
        trimmed.forEach { name ->
            when (val result = subjectRepository.addSubject(studentId, name)) {
                is Result.Error -> return result
                else -> Unit
            }
        }
        return Result.Success(Unit)
    }
}
