package com.studentcompanion.app.data.remote.rest

import com.studentcompanion.app.data.remote.rest.dto.LinkCodeResponse
import com.studentcompanion.app.data.remote.rest.dto.RestNoteDto
import com.studentcompanion.app.data.remote.rest.dto.RestPointsLedgerEntryDto
import com.studentcompanion.app.data.remote.rest.dto.RestSubjectDto
import com.studentcompanion.app.data.remote.rest.dto.RestTaskAttachmentDto
import com.studentcompanion.app.data.remote.rest.dto.RestTaskDto
import com.studentcompanion.app.data.remote.rest.dto.RestUserDto
import com.studentcompanion.app.domain.model.LinkCode
import com.studentcompanion.app.domain.model.Note
import com.studentcompanion.app.domain.model.PointsEntry
import com.studentcompanion.app.domain.model.Subject
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.model.TaskAttachment
import com.studentcompanion.app.domain.model.TaskStatus
import com.studentcompanion.app.domain.model.TaskType
import com.studentcompanion.app.domain.model.User
import com.studentcompanion.app.domain.model.UserRole
import java.time.Instant

// Mirrors data/mapper/Mappers.kt's role: the only place that knows the
// backend's JSON shapes (ISO-8601 instants, string-encoded BigInts).

fun RestUserDto.toDomain() = User(
    id = id,
    username = username,
    fullName = fullName,
    role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.STUDENT),
    schoolId = schoolId,
    classId = classId,
)

fun RestSubjectDto.toDomain() = Subject(
    id = id,
    name = name,
    studentId = studentId,
    colorHex = colorHex,
)

fun RestNoteDto.toDomain() = Note(
    id = id,
    studentId = studentId,
    subjectId = subjectId,
    subjectName = subjectName,
    title = title,
    body = body,
    createdAt = Instant.parse(createdAt),
    updatedAt = Instant.parse(updatedAt),
)

fun RestTaskAttachmentDto.toDomain() = TaskAttachment(
    id = id,
    url = url,
    sizeBytes = sizeBytes.toLong(),
)

fun RestTaskDto.toDomain() = Task(
    id = id,
    studentId = studentId,
    type = runCatching { TaskType.valueOf(type) }.getOrDefault(TaskType.HOMEWORK),
    subjectId = subjectId,
    subjectName = null, // the backend doesn't denormalize this for tasks (only notes)
    title = title,
    description = description,
    dueAt = Instant.parse(dueAt),
    status = runCatching { TaskStatus.valueOf(status) }.getOrDefault(TaskStatus.PENDING),
    pointsValue = pointsValue,
    studentComment = studentComment,
    teacherComment = teacherComment,
    attachments = attachments.map { it.toDomain() },
)

fun LinkCodeResponse.toDomain() = LinkCode(
    code = code,
    expiresAt = Instant.parse(expiresAt),
)

fun RestPointsLedgerEntryDto.toDomain() = PointsEntry(
    id = id,
    studentId = studentId,
    taskId = taskId,
    label = label,
    points = points,
    earnedAt = Instant.parse(earnedAt),
)
