package com.studentcompanion.app.data.mapper

import com.google.firebase.Timestamp
import com.studentcompanion.app.data.model.NoteDto
import com.studentcompanion.app.data.model.PointsEntryDto
import com.studentcompanion.app.data.model.TaskAttachmentDto
import com.studentcompanion.app.data.model.TaskDto
import com.studentcompanion.app.data.model.UserDto
import com.studentcompanion.app.domain.model.Note
import com.studentcompanion.app.domain.model.PointsEntry
import com.studentcompanion.app.domain.model.Task
import com.studentcompanion.app.domain.model.TaskAttachment
import com.studentcompanion.app.domain.model.TaskStatus
import com.studentcompanion.app.domain.model.TaskType
import com.studentcompanion.app.domain.model.User
import com.studentcompanion.app.domain.model.UserRole
import java.time.Instant

// Mappers are the ONLY place that knows about Firestore's Timestamp type
// or its DTO shapes — everything above the data layer speaks pure domain models.

private fun Timestamp.toInstant(): Instant = Instant.ofEpochSecond(seconds, nanoseconds.toLong())

fun UserDto.toDomain() = User(
    id = id,
    username = username,
    fullName = fullName,
    role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.STUDENT),
    schoolId = schoolId,
    classId = classId,
    parentId = parentId,
)

fun NoteDto.toDomain() = Note(
    id = id,
    studentId = studentId,
    subjectId = subjectId,
    subjectName = subjectName,
    title = title,
    body = body,
    createdAt = createdAt.toInstant(),
    updatedAt = updatedAt.toInstant(),
)

fun TaskAttachmentDto.toDomain() = TaskAttachment(id = id, url = url, sizeBytes = sizeBytes)

fun TaskDto.toDomain() = Task(
    id = id,
    studentId = studentId,
    type = runCatching { TaskType.valueOf(type) }.getOrDefault(TaskType.HOMEWORK),
    subjectId = subjectId,
    subjectName = subjectName,
    title = title,
    description = description,
    dueAt = dueAt.toInstant(),
    status = runCatching { TaskStatus.valueOf(status) }.getOrDefault(TaskStatus.PENDING),
    pointsValue = pointsValue,
    studentComment = studentComment,
    teacherComment = teacherComment,
    attachments = attachments.map { it.toDomain() },
)

fun PointsEntryDto.toDomain() = PointsEntry(
    id = id,
    studentId = studentId,
    taskId = taskId,
    label = label,
    points = points,
    earnedAt = earnedAt.toInstant(),
)
