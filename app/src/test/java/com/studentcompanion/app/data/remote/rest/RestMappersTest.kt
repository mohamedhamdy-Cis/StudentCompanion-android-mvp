package com.studentcompanion.app.data.remote.rest

import com.studentcompanion.app.data.remote.rest.dto.LinkCodeResponse
import com.studentcompanion.app.data.remote.rest.dto.RestSubjectDto
import com.studentcompanion.app.data.remote.rest.dto.RestTaskAttachmentDto
import com.studentcompanion.app.data.remote.rest.dto.RestTaskDto
import com.studentcompanion.app.data.remote.rest.dto.RestUserDto
import com.studentcompanion.app.domain.model.TaskStatus
import com.studentcompanion.app.domain.model.TaskType
import com.studentcompanion.app.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class RestMappersTest {

    @Test
    fun `RestUserDto with a valid role string maps to the matching enum`() {
        val dto = RestUserDto(
            id = "u1",
            username = "ahmad",
            fullName = "Ahmad",
            role = "PARENT",
            schoolId = null,
            classId = null,
        )
        assertEquals(UserRole.PARENT, dto.toDomain().role)
    }

    @Test
    fun `RestUserDto with an unknown role string defaults to STUDENT`() {
        val dto = RestUserDto(
            id = "u1",
            username = "ahmad",
            fullName = "Ahmad",
            role = "SOME_FUTURE_ROLE",
            schoolId = null,
            classId = null,
        )
        assertEquals(UserRole.STUDENT, dto.toDomain().role)
    }

    @Test
    fun `RestTaskDto maps type and status strings, parses the ISO instant, and converts attachments`() {
        val dto = RestTaskDto(
            id = "t1",
            studentId = "s1",
            type = "PRAYER",
            subjectId = null,
            title = "Fajr",
            description = "",
            dueAt = "2026-01-10T05:00:00.000Z",
            status = "APPROVED",
            pointsValue = 10,
            studentComment = null,
            teacherComment = null,
            attachments = listOf(RestTaskAttachmentDto(id = "a1", url = "https://x/y.jpg", sizeBytes = "2048")),
        )
        val task = dto.toDomain()

        assertEquals(TaskType.PRAYER, task.type)
        assertEquals(TaskStatus.APPROVED, task.status)
        assertEquals(Instant.parse("2026-01-10T05:00:00.000Z"), task.dueAt)
        assertEquals(1, task.attachments.size)
        assertEquals(2048L, task.attachments[0].sizeBytes)
    }

    @Test
    fun `RestTaskDto with an unrecognized status falls back to PENDING rather than throwing`() {
        val dto = RestTaskDto(
            id = "t1",
            studentId = "s1",
            type = "HOMEWORK",
            subjectId = null,
            title = "Essay",
            description = "",
            dueAt = "2026-01-10T05:00:00.000Z",
            status = "SOME_FUTURE_STATUS",
            pointsValue = 0,
            studentComment = null,
            teacherComment = null,
            attachments = emptyList(),
        )
        assertEquals(TaskStatus.PENDING, dto.toDomain().status)
    }

    @Test
    fun `RestSubjectDto maps colorHex straight through`() {
        val dto = RestSubjectDto(id = "sub1", studentId = "s1", name = "Math", colorHex = "#123456")
        val subject = dto.toDomain()
        assertEquals("#123456", subject.colorHex)
        assertEquals("Math", subject.name)
    }

    @Test
    fun `LinkCodeResponse parses the ISO expiresAt instant`() {
        val dto = LinkCodeResponse(code = "ABC123", expiresAt = "2026-01-11T05:00:00.000Z")
        val linkCode = dto.toDomain()
        assertEquals("ABC123", linkCode.code)
        assertEquals(Instant.parse("2026-01-11T05:00:00.000Z"), linkCode.expiresAt)
    }
}
